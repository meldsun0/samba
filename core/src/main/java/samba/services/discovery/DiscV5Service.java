/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package samba.services.discovery;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.crypto.SECP256K1.SecretKey;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import com.google.common.base.Preconditions;

import samba.config.DiscoveryConfig;
import samba.metrics.SambaMetricCategory;
import samba.schema.DefaultScheme;
import samba.store.KeyValueStore;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver.IPVersion;
import tech.pegasys.teku.service.serviceutils.Service;


public class DiscV5Service extends Service implements DiscoveryService {

    private static final Logger LOG = LogManager.getLogger();
    private static final String SEQ_NO_STORE_KEY = "local-enr-seqno";
    private static final Duration BOOTNODE_REFRESH_DELAY = Duration.ofSeconds(4);


    private final AsyncRunner asyncRunner;
    private final SecretKey localNodePrivateKey;
    private final DiscoverySystem discoverySystem;
    private final KeyValueStore<String, Bytes> kvStore;
    private final boolean supportsIpv6;
    private final List<NodeRecord> bootnodes;
    private final TalkHandler talkHandler;
    private volatile Cancellable bootnodeRefreshTask;
    public static final NodeRecordConverter DEFAULT_NODE_RECORD_CONVERTER = new NodeRecordConverter();

    public static DiscoverySystemBuilder createDefaultDiscoverySystemBuilder() {
        return new DiscoverySystemBuilder();
    }


    private final NodeRecordConverter nodeRecordConverter;


    public DiscV5Service(
            final MetricsSystem metricsSystem,
            final AsyncRunner asyncRunner,
            final DiscoveryConfig discoveryConfig,
            final KeyValueStore<String, Bytes> kvStore,
            final Bytes privateKey,
            final DiscoverySystemBuilder discoverySystemBuilder,
            final NodeRecordConverter nodeRecordConverter,
            final TalkHandler talkHandler) {
        this.asyncRunner = asyncRunner;
        this.localNodePrivateKey = SecretKeyParser.fromLibP2pPrivKey(privateKey);
        this.nodeRecordConverter = nodeRecordConverter;
        this.talkHandler = talkHandler;
        final SECP256K1.KeyPair keyPair = Functions.randomKeyPair(new Random(new Random().nextInt()));

        final List<String> networkInterfaces = List.of("0.0.0.0");
        Preconditions.checkState(networkInterfaces.size() == 1 || networkInterfaces.size() == 2, "The configured network interfaces must be either 1 or 2");


        if (networkInterfaces.size() == 1) {
            final String listenAddress = networkInterfaces.get(0);
            discoverySystemBuilder.listen(listenAddress, discoveryConfig.getListenUdpPort());
            this.supportsIpv6 = IPVersionResolver.resolve(listenAddress) == IPVersion.IP_V6;
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



        //this.bootnodes = discoveryConfig.getBootnodes().stream().map(NodeRecordFactory.DEFAULT::fromEnr).toList();
        this.bootnodes = discoveryConfig.getBootnodes();
        NodeRecord localNodeRecord = createNodeRecord(keyPair,"23.44.56.78",Integer.parseInt("9001"));

        this.discoverySystem =
                discoverySystemBuilder
                        .secretKey(localNodePrivateKey)
                        .bootnodes(bootnodes)
                        .localNodeRecord(localNodeRecord)
                        .localNodeRecordListener(this::localNodeRecordUpdated)
                        .build();
                        // .talkHandler(talkHandler)
        this.kvStore = kvStore;
        NodeRecord myNode = discoverySystem.getLocalNodeRecord();


        metricsSystem.createIntegerGauge(
                SambaMetricCategory.DISCOVERY,
                "live_nodes_current",
                "Current number of live nodes tracked by the discovery system",
                () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());
    }


    private void localNodeRecordUpdated(final NodeRecord oldRecord, final NodeRecord newRecord) {
//        System.out.println(
//                                "New active node: "
//                                        + newRecord.getNodeId()
//                                        + " @ "
//                                        + newRecord.getUdpAddress().map(InetSocketAddress::toString).orElse("<unknown>"));
        kvStore.put(SEQ_NO_STORE_KEY, newRecord.getSeq().toBytes());
    }

    @Override
    protected SafeFuture<?> doStart() {
        return SafeFuture.of(discoverySystem.start())
                .thenRun(
                        () ->{
                                this.bootnodeRefreshTask =
                                        asyncRunner.runWithFixedDelay(
                                                this::pingBootnodes,
                                                BOOTNODE_REFRESH_DELAY,
                                                error -> LOG.error("Failed to contact discovery bootnodes", error));
                        }
                );
    }

    private void pingBootnodes() {
        System.out.println("pinging");
        bootnodes.forEach(
                bootnode ->
                        SafeFuture.of(discoverySystem.ping(bootnode).whenComplete((a,b) -> System.out.println(a+ ""+ b)))
                                .whenComplete((a,b) -> System.out.println(a))
                                .finish(error -> LOG.info("Bootnode {} is unresponsive", bootnode)));
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

    @Override
    public Stream<DiscoveryPeer> streamKnownPeers() {
        final SchemaDefinitions schemaDefinitions = new DefaultScheme();
        return activeNodes()
                .flatMap(
                        node ->
                                nodeRecordConverter.convertToDiscoveryPeer(node, supportsIpv6, schemaDefinitions).stream());
    }

    @Override
    public SafeFuture<Collection<DiscoveryPeer>> searchForPeers() {
        return SafeFuture.of(discoverySystem.searchForNewPeers())
                // Current version of discovery doesn't return the found peers but next version will
                .<Collection<NodeRecord>>thenApply(__ -> emptyList())
                .thenApply(this::convertToDiscoveryPeers);
    }

    private List<DiscoveryPeer> convertToDiscoveryPeers(final Collection<NodeRecord> foundNodes) {
        LOG.debug("Found {} nodes prior to filtering", foundNodes.size());
        final SchemaDefinitions schemaDefinitions = new DefaultScheme();
        return foundNodes.stream()
                .flatMap(
                        nodeRecord ->
                                nodeRecordConverter.convertToDiscoveryPeer(nodeRecord, supportsIpv6, schemaDefinitions).stream())
                .toList();
    }

    @Override
    public Optional<String> getEnr() {
        return Optional.of(discoverySystem.getLocalNodeRecord().asEnr());
    }

    @Override
    public Optional<Bytes> getNodeId() {
        return Optional.of(discoverySystem.getLocalNodeRecord().getNodeId());
    }

    @Override
    public Optional<List<String>> getDiscoveryAddresses() {
        final NodeRecord nodeRecord = discoverySystem.getLocalNodeRecord();
        final List<InetSocketAddress> updAddresses = new ArrayList<>();
        nodeRecord.getUdpAddress().ifPresent(updAddresses::add);
        nodeRecord.getUdp6Address().ifPresent(updAddresses::add);
        if (updAddresses.isEmpty()) {
            return Optional.empty();
        }
        final List<String> discoveryAddresses =
                updAddresses.stream()
                        .map(
                                updAddress -> {
                                    final DiscoveryPeer discoveryPeer = new DiscoveryPeer((Bytes) nodeRecord.get(EnrField.PKEY_SECP256K1), updAddress);
                                    //return MultiaddrUtil.fromDiscoveryPeerAsUdp(discoveryPeer).toString();
                                    return discoveryPeer.toString();
                                })
                        .toList();
        return Optional.of(discoveryAddresses);
    }

    @Override
    public void updateCustomENRField(final String fieldName, final Bytes value) {
        discoverySystem.updateCustomFieldValue(fieldName, value);
    }

    private Stream<NodeRecord> activeNodes() {
        return discoverySystem.streamLiveNodes();
    }


    private NodeRecord createNodeRecord(final SECP256K1.KeyPair keyPair , final String ip, final int port) {
        return new NodeRecordBuilder()
                .secretKey(keyPair.secretKey())
                .address(ip, port)
                .build();
    }
}
