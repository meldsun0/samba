package samba.network;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public interface PeerClient {


    public CompletableFuture<Bytes> sendMessage(NodeRecord nodeRecord, Bytes protocol, Bytes request);




    SafeFuture<Collection<NodeRecord>> streamLiveNodes();


//
//    @Override
//    public SafeFuture<Collection<DiscoveryPeer>> searchForPeers() {
//        return SafeFuture.of(discoverySystem.searchForNewPeers())
//                // Current version of discovery doesn't return the found peers but next version will
//                .<Collection<NodeRecord>>thenApply(__ -> emptyList())
//                .thenApply(this::convertToDiscoveryPeers);
//    }
//
//    private List<DiscoveryPeer> convertToDiscoveryPeers(final Collection<NodeRecord> foundNodes) {
//        LOG.debug("Found {} nodes prior to filtering", foundNodes.size());
//        final SchemaDefinitions schemaDefinitions =
//                currentSchemaDefinitionsSupplier.getSchemaDefinitions();
//        return foundNodes.stream()
//                .flatMap(
//                        nodeRecord ->
//                                nodeRecordConverter
//                                        .convertToDiscoveryPeer(nodeRecord, supportsIpv6, schemaDefinitions)
//                                        .stream())
//                .toList();
//    }

}



