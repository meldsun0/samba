package samba.services.discovery;

import samba.config.DiscoveryConfig;
import samba.domain.messages.IncomingRequestHandler;
import samba.metrics.SambaMetricCategory;
import samba.util.MultiaddrUtil;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.multiformats.Protocol;
import io.libp2p.core.multiformats.Protocol.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.AddressAccessPolicy;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;
import tech.pegasys.teku.service.serviceutils.Service;

public class Discv5Service extends Service implements Discv5Client {

  private static final Logger LOG = LogManager.getLogger();
  private final DiscoverySystem discoverySystem;
  private final AsyncRunner asyncRunner;
  private volatile Cancellable bootnodeRefreshTask;

  private final boolean supportsIpv6;

  private static volatile Bytes local_enr_seqno;

  public Discv5Service(
      final MetricsSystem metricsSystem,
      final AsyncRunner asyncRunner,
      final DiscoveryConfig discoveryConfig,
      final SECP256K1.SecretKey secretKey,
      final IncomingRequestHandler incomingRequestProcessor) {

    this.asyncRunner = asyncRunner;
    final DiscoverySystemBuilder discoverySystemBuilder = new DiscoverySystemBuilder();
    final List<String> networkInterfaces = discoveryConfig.getNetworkInterfaces();

    Preconditions.checkState(
        networkInterfaces.size() == 1 || networkInterfaces.size() == 2,
        "The configured network interfaces must be either 1 or 2");

    if (networkInterfaces.size() == 1) {
      final String listenAddress = networkInterfaces.get(0);
      discoverySystemBuilder.listen(listenAddress, discoveryConfig.getListenUdpPort());
      this.supportsIpv6 =
          IPVersionResolver.resolve(listenAddress) == IPVersionResolver.IPVersion.IP_V6;
    } else {
      final InetSocketAddress[] listenAddresses =
          getDualStackNetworkInterfaces(discoverySystemBuilder, networkInterfaces, discoveryConfig);
      discoverySystemBuilder.listen(listenAddresses);
      this.supportsIpv6 = true;
    }

    // TODO save seqNo Locally
    // final UInt64 seqNo =local_enr_seqno.map(UInt64::fromBytes).orElse(UInt64.ZERO).add(1);
    final UInt64 seqNo = UInt64.ZERO.add(1);
    final NodeRecordBuilder nodeRecordBuilder =
        new NodeRecordBuilder().secretKey(secretKey).seq(seqNo);

    this.addAdvertisedIpToNodeRecordBuilder(discoveryConfig, nodeRecordBuilder);

    this.discoverySystem =
        discoverySystemBuilder
            .secretKey(secretKey)
            .bootnodes(discoveryConfig.getBootnodes())
            .localNodeRecord(nodeRecordBuilder.build())
            .localNodeRecordListener(this::createLocalNodeRecordListener)
            .talkHandler(incomingRequestProcessor)
            .addressAccessPolicy(AddressAccessPolicy.ALLOW_ALL) // TODO check this.
            .build();

    metricsSystem.createIntegerGauge(
        SambaMetricCategory.DISCOVERY,
        "live_nodes_current",
        "Current number of live nodes tracked by the discovery system",
        () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());

    LOG.info("ENR :{}", this.getHomeNodeRecord());
  }

  private void createLocalNodeRecordListener(NodeRecord oldRecord, NodeRecord newRecord) {
    local_enr_seqno = newRecord.getSeq().toBytes();
  }

  @Override
  public CompletableFuture<Bytes> sendDisv5Message(
      NodeRecord nodeRecord, Bytes protocol, Bytes request) {
    return this.discoverySystem
        .talk(nodeRecord, protocol, request)
        .exceptionallyCompose(this::handleError);
  }

  private CompletionStage<Bytes> handleError(Throwable error) {
    LOG.trace("Something when wrong when sending a Discv5 message");
    return SafeFuture.failedFuture(error);
  }

  @Override
  public SafeFuture<Collection<NodeRecord>> streamLiveNodes() {
    // TODO maybe searchForNewPeers?
    return SafeFuture.of(() -> discoverySystem.streamLiveNodes().toList());
  }

  @Override
  public Optional<String> lookupEnr(final UInt256 nodeId) {
    final Optional<NodeRecord> maybeNodeRecord = discoverySystem.lookupNode(nodeId.toBytes());
    return maybeNodeRecord.map(NodeRecord::asEnr);
  }

  @Override
  public Optional<Bytes> getNodeId() {
    return Optional.of(discoverySystem.getLocalNodeRecord().getNodeId());
  }

  @Override
  public NodeRecord getHomeNodeRecord() {
    return this.discoverySystem.getLocalNodeRecord();
  }

  @Override
  public Optional<String> getEnr() {
    return Optional.of(discoverySystem.getLocalNodeRecord().asEnr());
  }

  @Override
  public UInt64 getEnrSeq() {
    return discoverySystem.getLocalNodeRecord().getSeq();
  }

  @Override
  public CompletableFuture<Collection<NodeRecord>> sendDiscv5FindNodes(
      NodeRecord nodeRecord, List<Integer> distances) {
    return discoverySystem.findNodes(nodeRecord, distances);
  }

  private Stream<NodeRecord> converToPeer(NodeRecord nodeRecord) {
    // TODO convert to our on definition of Node
    return null;
  }

  @Override
  protected SafeFuture<?> doStart() {
    return SafeFuture.of(discoverySystem.start());
  }

  @Override
  protected SafeFuture<?> doStop() {
    final Cancellable refreshTask = this.bootnodeRefreshTask;
    this.bootnodeRefreshTask = null;
    if (refreshTask != null) {
      refreshTask.cancel();
    }
    discoverySystem.stop();
    return SafeFuture.completedFuture(null);
  }

  public void updateCustomENRField(final String fieldName, final Bytes value) {
    discoverySystem.updateCustomFieldValue(fieldName, value);
  }

  @Override
  public NodeRecord updateNodeRecordSocket(Multiaddr multiaddr) {
    final boolean isIpV6 = multiaddr.has(Protocol.IP6);
    final Bytes address =
        MultiaddrUtil.getMultiAddrValue(multiaddr, isIpV6 ? Protocol.IP6 : Protocol.IP4);
    this.updateCustomENRField(isIpV6 ? EnrField.IP_V6 : EnrField.IP_V4, address);
    if (multiaddr.has(Protocol.UDP)) {
      this.updateCustomENRField(
          isIpV6 ? EnrField.UDP_V6 : EnrField.UDP,
          MultiaddrUtil.getMultiAddrValue(multiaddr, Protocol.UDP));
    }
    if (multiaddr.has(Protocol.TCP)) {
      this.updateCustomENRField(
          isIpV6 ? EnrField.TCP_V6 : EnrField.TCP,
          MultiaddrUtil.getMultiAddrValue(multiaddr, Protocol.TCP));
    }
    return this.getHomeNodeRecord();
  }

  private InetSocketAddress[] getDualStackNetworkInterfaces(
      final DiscoverySystemBuilder discoverySystemBuilder,
      final List<String> networkInterfaces,
      final DiscoveryConfig discoveryConfig) {
    return networkInterfaces.stream()
        .map(
            networkInterface -> {
              final int listenUdpPort =
                  switch (IPVersionResolver.resolve(networkInterface)) {
                    case IP_V4 -> discoveryConfig.getListenUdpPort();
                    case IP_V6 -> discoveryConfig.getListenUdpPortIpv6();
                  };
              return new InetSocketAddress(networkInterface, listenUdpPort);
            })
        .toArray(InetSocketAddress[]::new);
  }

  private void addAdvertisedIpToNodeRecordBuilder(
      final DiscoveryConfig discoveryConfig, final NodeRecordBuilder nodeRecordBuilder) {
    boolean hasUserExplicitlySetAdvertisedIps = false;
    if (hasUserExplicitlySetAdvertisedIps) { // TODO fix when acceptin parameters.
      final List<String> advertisedIps = discoveryConfig.getAdvertisedIps();
      Preconditions.checkState(
          advertisedIps.size() == 1 || advertisedIps.size() == 2,
          "The configured advertised IPs must be either 1 or 2");
      if (advertisedIps.size() == 1) {
        nodeRecordBuilder.address(
            advertisedIps.get(0),
            discoveryConfig.getAdvertisedUdpPort(),
            discoveryConfig.getAdvertisedPort());
      } else {
        advertisedIps.forEach(
            advertisedIp -> {
              final IPVersionResolver.IPVersion ipVersion = IPVersionResolver.resolve(advertisedIp);
              final int advertisedUdpPort = discoveryConfig.getAdvertisedUdpPort(ipVersion);
              final int advertisedTcpPort = discoveryConfig.getAdvertisedTcpPort(ipVersion);
              nodeRecordBuilder.address(advertisedIp, advertisedUdpPort, advertisedTcpPort);
            });
      }
    }
  }
}
