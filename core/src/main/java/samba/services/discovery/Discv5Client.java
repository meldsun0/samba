package samba.services.discovery;


import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Discv5Client {


    CompletableFuture<Bytes> sendDisv5Message(NodeRecord nodeRecord, Bytes protocol, Bytes request);

    SafeFuture<Collection<NodeRecord>> streamLiveNodes();

    Optional<Bytes> getNodeId();

    Optional<String> getEnr();

    UInt64 getEnrSeq();

    CompletableFuture<Collection<NodeRecord>> sendDiscv5FindNodes(NodeRecord nodeRecord, List<Integer> distances);

    void updateCustomENRField(final String fieldName, final Bytes value);



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



