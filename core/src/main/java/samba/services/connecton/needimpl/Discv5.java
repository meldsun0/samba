package samba.services.connecton.needimpl;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.DiscoveryConfig;
import samba.metrics.SambaMetricCategory;
import samba.network.PeerClient;
import samba.services.discovery.SecretKeyParser;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;
import tech.pegasys.teku.service.serviceutils.Service;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class Discv5  extends Service implements PeerClient {

    private static final Logger LOG = LogManager.getLogger();
    private static final Duration BOOTNODE_REFRESH_DELAY = Duration.ofSeconds(4);

    private final DiscoverySystem discoverySystem;
    private final List<NodeRecord> bootnodes;
    private final boolean supportsIpv6;
    private final SECP256K1.SecretKey localNodePrivateKey;



    public Discv5(final DiscoveryConfig discoveryConfig, final List bootnodes, final Bytes privateKey,
                  final MetricsSystem metricsSystem){
        //add node Record converter.
        this.bootnodes = bootnodes;
        this.localNodePrivateKey = SecretKeyParser.fromLibP2pPrivKey(privateKey);


        final SECP256K1.KeyPair keyPair = Functions.randomKeyPair(new Random(new Random().nextInt()));

        final List<String> networkInterfaces = List.of("0.0.0.0");
        Preconditions.checkState(networkInterfaces.size() == 1 || networkInterfaces.size() == 2, "The configured network interfaces must be either 1 or 2");
        DiscoverySystemBuilder discoverySystemBuilder =  new DiscoverySystemBuilder();

        if (networkInterfaces.size() == 1) {
            final String listenAddress = networkInterfaces.get(0);
            discoverySystemBuilder.listen(listenAddress, discoveryConfig.getListenUdpPort());
            this.supportsIpv6 = IPVersionResolver.resolve(listenAddress) == IPVersionResolver.IPVersion.IP_V6;
        } else {
            // IPv4 and IPv6 (dual-stack)
            final InetSocketAddress[] listenAddresses =
                    networkInterfaces.stream()
                            .map(
                                    networkInterface -> {
                                        final int listenUdpPort =
                                                switch (IPVersionResolver.resolve(networkInterface)) {
                                                    case IP_V4 -> discoveryConfig.getListenUdpPort();
                                                    case IP_V6 -> discoveryConfig.getListenUpdPortIpv6();
                                                };
                                        return new InetSocketAddress(networkInterface, listenUdpPort);
                                    })
                            .toArray(InetSocketAddress[]::new);
            discoverySystemBuilder.listen(listenAddresses);
            this.supportsIpv6 = true;
        }




        this.discoverySystem =
                discoverySystemBuilder
                        .secretKey(localNodePrivateKey)
                        .bootnodes(bootnodes)
                        .localNodeRecord(createNodeRecord(keyPair,"23.44.56.78",Integer.parseInt("9001")))
                        .localNodeRecordListener(this::createLocalNodeRecordListener)
                        .build();

        NodeRecord myNode = discoverySystem.getLocalNodeRecord();


        metricsSystem.createIntegerGauge(
                SambaMetricCategory.DISCOVERY,
                "live_nodes_current",
                "Current number of live nodes tracked by the discovery system",
                () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());
    }

    private void createLocalNodeRecordListener(NodeRecord nodeRecord, NodeRecord nodeRecord1) {

    }


    private NodeRecord createNodeRecord(final SECP256K1.KeyPair keyPair , final String ip, final int port) {
        return new NodeRecordBuilder()
                .secretKey(keyPair.secretKey())
                .address(ip, port)
                .build();
    }


    @Override
    public CompletableFuture<Bytes> sendMessage(NodeRecord nodeRecord, Bytes protocol, Bytes request) {
        return this.discoverySystem.talk(nodeRecord, protocol, request);
    }

    @Override
    public SafeFuture<Collection<NodeRecord>> streamLiveNodes() {
        //TODO maybe searchForNewPeers?
        return SafeFuture.of(
                () -> {
                    Stream<NodeRecord> nodes =discoverySystem.streamLiveNodes(); //   .thenApply(this::convertToDiscoveryPeers);
                    return nodes.toList();
                });

    }

    private Stream<NodeRecord> converToPeer(NodeRecord nodeRecord) {
        return null;
    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.trace("Starting DiscV5 service");
        return SafeFuture.of(discoverySystem.start());
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }




}
