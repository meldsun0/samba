package samba.services.discovery;


import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Discv5Client {


    public CompletableFuture<Bytes> sendDisV5Message(NodeRecord nodeRecord, Bytes protocol, Bytes request);


    SafeFuture<Collection<NodeRecord>> streamLiveNodes();

    public Optional<Bytes> getNodeId();


    public Optional<String> getEnr();

    public void updateCustomENRField(final String fieldName, final Bytes value);



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



