package samba.domain.dht;

import lombok.NonNull;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;
import samba.domain.node.NodeDistanceCalculator;
import samba.domain.node.Node;

import java.util.Optional;
import java.util.stream.Stream;

public class NodeTable {
    private static final int N_BUCKETS = 256;
    private static final int DEFAULT_BUCKET_SIZE = 16;

    private Bucket[] buckets;
    private Node owner;


    public NodeTable(@NonNull final Node owner){
        this.buckets = Stream.generate(() -> new Bucket(DEFAULT_BUCKET_SIZE)).limit(N_BUCKETS + 1).toArray(Bucket[]::new);
        this.owner = owner;
    }


    /**
     * Returns a Node of the given nodeId
     *
     * @param nodeId The nodeId to query.
     * @return An option with the Node found or an empty optional if the Node was not found
     */
    public Optional<Node> get(final Bytes nodeId) {
        final int distance = this.calculateDistanceFrom(nodeId);
        return this.buckets[distance].get(nodeId).map(BucketEntry::getNode);
    }


    public void add(final Node node) {
        //TODO validations
        final int distance = this.calculateDistanceFrom(node.getId());
        // Avoid adding ourselves to the node table.
        if (distance == 0) {
            //TODO
        }
        final Bucket bucket = this.buckets[distance];
        try {
            bucket.add(this.createBucketEntry(node));
        } catch (final IllegalArgumentException ex) {
            //TODO
        }
    }

    /**
     * Calculates the XOR distance between the keccak-256 of the ownerNodeId and the provided
     * @param nodeId The destination node.
     * @return The distance.
     */
    private int calculateDistanceFrom(final Bytes nodeId) {
        return NodeDistanceCalculator.calculateDistance(this.owner.keccak256(),  Hash.keccak256(nodeId));
    }

    private BucketEntry createBucketEntry(final Node node){
        return new BucketEntry(node);
    }
}
