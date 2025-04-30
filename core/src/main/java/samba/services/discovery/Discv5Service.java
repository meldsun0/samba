package samba.services.discovery;

import samba.config.DiscoveryConfig;
import samba.domain.messages.IncomingRequestTalkHandler;
import samba.metrics.SambaMetricCategory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.AddressAccessPolicy;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.MutableDiscoverySystem;
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
  private final MutableDiscoverySystem discoverySystem;
  private final AsyncRunner asyncRunner;
  private volatile Cancellable bootnodeRefreshTask;

  private final boolean supportsIpv6;

  private static volatile Bytes local_enr_seqno;

  public Discv5Service(
      final MetricsSystem metricsSystem,
      final AsyncRunner asyncRunner,
      final DiscoveryConfig discoveryConfig,
      final SECP256K1.SecretKey secretKey,
      final IncomingRequestTalkHandler incomingRequestTalkHandler) {

    this.asyncRunner = asyncRunner;
    final DiscoverySystemBuilder discoverySystemBuilder = new DiscoverySystemBuilder();
    final List<String> networkInterfaces = discoveryConfig.getNetworkInterfaces();

    Preconditions.checkState(
        networkInterfaces.size() == 1 || networkInterfaces.size() == 2,
        "The configured network interfaces must be either 1 or 2");

    final UInt64 seqNo = UInt64.ZERO.add(1);
    final NodeRecordBuilder nodeRecordBuilder =
        this.createNodeRecordBuilder(discoveryConfig, secretKey, seqNo);

    if (networkInterfaces.size() == 1) {
      final String listenAddress = networkInterfaces.getFirst();
      discoverySystemBuilder.listen(listenAddress, discoveryConfig.getListenUDPPortIpv4());
      this.supportsIpv6 =
          IPVersionResolver.resolve(listenAddress) == IPVersionResolver.IPVersion.IP_V6;
    } else {
      final InetSocketAddress[] listenAddresses =
          discoveryConfig.getDualStackListenNetworkInterfaces(networkInterfaces);
      discoverySystemBuilder.listen(listenAddresses);
      this.supportsIpv6 = true;
    }

    if (discoveryConfig.hasUserExplicitlySetAdvertisedIps()) {
      final List<String> advertisedIps = discoveryConfig.getAdvertisedIps();
      Preconditions.checkState(
          advertisedIps.size() == 1 || advertisedIps.size() == 2,
          "The configured advertised IPs must be either 1 or 2");
      if (advertisedIps.size() == 1) {
        nodeRecordBuilder.address(
            advertisedIps.getFirst(),
            discoveryConfig.getAdvertisedUDPPortIpv4(),
            discoveryConfig.getAdvertisedTCPPortIpv4());
      } else {
        // IPv4 and IPv6 (dual-stack)
        advertisedIps.forEach(
            advertisedIp ->
                nodeRecordBuilder.address(
                    advertisedIp,
                    discoveryConfig.getAdvertisedUDPPort(advertisedIp),
                    discoveryConfig.getAdvertisedTCPPort(advertisedIp)));
      }
    } else {
      if (networkInterfaces.size() == 1) {
        final String listenAddress = networkInterfaces.getFirst();
        nodeRecordBuilder.address(
            listenAddress,
            discoveryConfig.getListenUDPPortIpv4(),
            discoveryConfig.getListenTCPPortIpv4());
      } else {
        discoveryConfig
            .getIps()
            .forEach(
                ip ->
                    nodeRecordBuilder.address(
                        ip,
                        discoveryConfig.getListenUDPPort(ip),
                        discoveryConfig.getListenTCPPort(ip)));
      }
    }

    this.discoverySystem =
        createDiscoverySystem(
            discoveryConfig,
            secretKey,
            incomingRequestTalkHandler,
            discoverySystemBuilder,
            nodeRecordBuilder);

    metricsSystem.createIntegerGauge(
        SambaMetricCategory.DISCOVERY,
        "live_nodes_current",
        "Current number of live nodes tracked by the discovery system",
        () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());

    LOG.info("ENR :{}", this.getHomeNodeRecord());
  }

  private MutableDiscoverySystem createDiscoverySystem(
      DiscoveryConfig discoveryConfig,
      SECP256K1.SecretKey secretKey,
      IncomingRequestTalkHandler incomingRequestTalkHandler,
      DiscoverySystemBuilder discoverySystemBuilder,
      NodeRecordBuilder nodeRecordBuilder) {
    return discoverySystemBuilder
        .secretKey(secretKey)
        .bootnodes(discoveryConfig.getBootnodes())
        .localNodeRecord(nodeRecordBuilder.build())
        .localNodeRecordListener(this::createLocalNodeRecordListener)
        .talkHandler(incomingRequestTalkHandler)
        .addressAccessPolicy(AddressAccessPolicy.ALLOW_ALL) // TODO check this.
        .buildMutable();
  }

  private NodeRecordBuilder createNodeRecordBuilder(
      DiscoveryConfig discoveryConfig, SECP256K1.SecretKey secretKey, UInt64 seqNo) {
    return new NodeRecordBuilder()
        .secretKey(secretKey)
        .seq(seqNo)
        .customField(discoveryConfig.getClientKey(), discoveryConfig.getClientValue());
  }

  private void createLocalNodeRecordListener(NodeRecord oldRecord, NodeRecord newRecord) {
    local_enr_seqno = newRecord.getSeq().toBytes();
    LOG.info("LocalNodeRecord updated : {}", newRecord);
  }

  @Override
  public CompletableFuture<Bytes> sendDiscv5Message(
      NodeRecord nodeRecord, Bytes protocol, Bytes request) {
    return this.discoverySystem
        .talk(nodeRecord, protocol, request)
        .exceptionallyCompose(this::handleError);
  }

  private CompletionStage<Bytes> handleError(Throwable error) {
    LOG.warn("Something when wrong when sending a Discv5 message");
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
  public CompletableFuture<Void> ping(NodeRecord nodeRecord) {
    return this.discoverySystem.ping(nodeRecord);
  }

  @Override
  public CompletableFuture<Collection<NodeRecord>> findNodes(
      NodeRecord nodeRecord, List<Integer> distances) {
    return this.discoverySystem.findNodes(nodeRecord, distances);
  }

  @Override
  public CompletableFuture<Bytes> talk(NodeRecord nodeRecord, Bytes protocol, Bytes request) {
    return this.discoverySystem.talk(nodeRecord, protocol, request);
  }

  @Override
  public List<List<NodeRecord>> getRoutingTable() {
    return this.discoverySystem.getNodeRecordBuckets();
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
  public boolean updateEnrSocket(InetSocketAddress socketAddress, boolean isTCP) {
    final Bytes address = Bytes.wrap(socketAddress.getAddress().getAddress());
    final Bytes port = Bytes.ofUnsignedInt(socketAddress.getPort());
    final IPVersionResolver.IPVersion ipVersion = IPVersionResolver.resolve(socketAddress);

    String ipField;
    String portField;

    switch (ipVersion) {
      case IP_V4 -> {
        ipField = EnrField.IP_V4;
        portField = isTCP ? EnrField.TCP : EnrField.UDP;
      }
      case IP_V6 -> {
        ipField = EnrField.IP_V6;
        portField = isTCP ? EnrField.TCP_V6 : EnrField.UDP_V6;
      }
      default -> throw new IllegalArgumentException("Unsupported IP version: " + ipVersion);
    }

    this.updateCustomENRField(ipField, address);
    this.updateCustomENRField(portField, port);

    NodeRecord nodeRecord = this.getHomeNodeRecord();

    Bytes updatedIp = (Bytes) nodeRecord.get(ipField);
    Bytes updatedPort = (Bytes) nodeRecord.get(portField);

    return address.equals(updatedIp) && port.equals(updatedPort);
  }
}
