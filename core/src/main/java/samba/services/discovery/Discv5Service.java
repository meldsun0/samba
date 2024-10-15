package samba.services.discovery;

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
import samba.domain.messages.handler.IncomingRequestHandler;
import samba.metrics.SambaMetricCategory;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;



public class Discv5Service extends Service implements Discv5Client {

    private static final Logger LOG = LogManager.getLogger();
    private final DiscoverySystem discoverySystem;
    private final NodeRecord localNodeRecord;
//    private final boolean supportsIpv6;
//    private final SECP256K1.SecretKey localNodePrivateKey;

    private final AsyncRunner asyncRunner;
    private volatile Cancellable bootnodeRefreshTask;

    public Discv5Service(final MetricsSystem metricsSystem,
                         final AsyncRunner asyncRunner,
                         final DiscoveryConfig discoveryConfig,
                         final Bytes privateKey,
                         final IncomingRequestHandler incomingRequestProcessor) {
        //add node Record converter.
        this.asyncRunner = asyncRunner;
        //  this.localNodePrivateKey = SECP256K1.SecretKey.fromInteger(new BigInteger(privateKey.toArrayUnsafe()));


        final SECP256K1.KeyPair keyPair = Functions.randomKeyPair(new Random(new Random().nextInt()));

        final List<String> networkInterfaces = List.of("0.0.0.0");
        Preconditions.checkState(networkInterfaces.size() == 1 || networkInterfaces.size() == 2, "The configured network interfaces must be either 1 or 2");


//        if (networkInterfaces.size() == 1) {
//            final String listenAddress = networkInterfaces.get(0);
//            discoverySystemBuilder.listen(listenAddress, discoveryConfig.getListenUdpPort());
//            this.supportsIpv6 = IPVersionResolver.resolve(listenAddress) == IPVersionResolver.IPVersion.IP_V6;
//        } else {
//            // IPv4 and IPv6 (dual-stack)
//            final InetSocketAddress[] listenAddresses =
//                    networkInterfaces.stream()
//                            .map(
//                                    networkInterface -> {
//                                        final int listenUdpPort =
//                                                switch (IPVersionResolver.resolve(networkInterface)) {
//                                                    case IP_V4 -> discoveryConfig.getListenUdpPort();
//                                                    case IP_V6 -> discoveryConfig.getListenUpdPortIpv6();
//                                                };
//                                        return new InetSocketAddress(networkInterface, listenUdpPort);
//                                    })
//                            .toArray(InetSocketAddress[]::new);
//            discoverySystemBuilder.listen(listenAddresses);
//            this.supportsIpv6 = true;
//        }


        this.discoverySystem =
                new DiscoverySystemBuilder()
                        .listen("0.0.0.0", 9090)
                        .secretKey(keyPair.secretKey())
                        .bootnodes(discoveryConfig.getBootnodes())
                        .localNodeRecord(createNodeRecord(keyPair, "0.0.0.0", 9090)) // "181.28.127.143", Integer.parseInt("9001")))
                        .localNodeRecordListener(this::createLocalNodeRecordListener)
                        .talkHandler(incomingRequestProcessor).build();

        this.localNodeRecord = discoverySystem.getLocalNodeRecord();


        metricsSystem.createIntegerGauge(
                SambaMetricCategory.DISCOVERY,
                "live_nodes_current",
                "Current number of live nodes tracked by the discovery system",
                () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());
    }

    private void createLocalNodeRecordListener(NodeRecord nodeRecord, NodeRecord nodeRecord1) {
        LOG.trace("Implement createLocalNodeRecordListener");
    }


    private NodeRecord createNodeRecord(final SECP256K1.KeyPair keyPair, final String ip, final int port) {
        return new NodeRecordBuilder()
                .secretKey(keyPair.secretKey())
                .address(ip, port)
                .build();
    }


    @Override
    public CompletableFuture<Bytes> sendDisV5Message(NodeRecord nodeRecord, Bytes protocol, Bytes request) {
        return this.discoverySystem.talk(nodeRecord, protocol, request);
    }

    @Override
    public SafeFuture<Collection<NodeRecord>> streamLiveNodes() {
        //TODO maybe searchForNewPeers?
        return SafeFuture.of(()->discoverySystem.streamLiveNodes().toList()); //   .thenApply(this::converToPeer);
    }

    @Override
    public Optional<Bytes> getNodeId() {
        return Optional.of(discoverySystem.getLocalNodeRecord().getNodeId());
    }

    private Stream<NodeRecord> converToPeer(NodeRecord nodeRecord) {
        //TODO convert to our on definition of Node
        return null;
    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.info("Starting DiscV5 service");
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

    public Optional<String> getEnr() {
        return Optional.of(discoverySystem.getLocalNodeRecord().asEnr());
    }

    public void updateCustomENRField(final String fieldName, final Bytes value) {
        discoverySystem.updateCustomFieldValue(fieldName, value);
    }
}