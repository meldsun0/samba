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

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.crypto.SECP256K1.SecretKey;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;
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


import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;


public class DiscV5Service extends Service {


    private static final String SEQ_NO_STORE_KEY = "local-enr-seqno";



    private final AsyncRunner asyncRunner;
    private final SecretKey localNodePrivateKey;

    private final KeyValueStore<String, Bytes> kvStore;
    private final boolean supportsIpv6;

    private volatile Cancellable bootnodeRefreshTask;
    public static final NodeRecordConverter DEFAULT_NODE_RECORD_CONVERTER = new NodeRecordConverter();




    private final NodeRecordConverter nodeRecordConverter;


    public DiscV5Service(
            final MetricsSystem metricsSystem,
            final AsyncRunner asyncRunner,
            final DiscoveryConfig discoveryConfig,
            final KeyValueStore<String, Bytes> kvStore,
            final Bytes privateKey,
            final DiscoverySystemBuilder discoverySystemBuilder,
            final NodeRecordConverter nodeRecordConverter) {
        this.asyncRunner = asyncRunner;


        final SECP256K1.KeyPair keyPair = Functions.randomKeyPair(new Random(new Random().nextInt()));




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





    private List<DiscoveryPeer> convertToDiscoveryPeers(final Collection<NodeRecord> foundNodes) {
        LOG.debug("Found {} nodes prior to filtering", foundNodes.size());
        final SchemaDefinitions schemaDefinitions = new DefaultScheme();
        return foundNodes.stream()
                .flatMap(
                        nodeRecord ->
                                nodeRecordConverter.convertToDiscoveryPeer(nodeRecord, supportsIpv6, schemaDefinitions).stream())
                .toList();
    }


    public Optional<String> getEnr() {
        return Optional.of(discoverySystem.getLocalNodeRecord().asEnr());
    }


    public Optional<Bytes> getNodeId() {
        return Optional.of(discoverySystem.getLocalNodeRecord().getNodeId());
    }


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


    public void updateCustomENRField(final String fieldName, final Bytes value) {
        discoverySystem.updateCustomFieldValue(fieldName, value);
    }




}
