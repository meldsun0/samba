/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.domain.routingtable;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.util.Functions;

import java.time.Clock;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NodeTable {

    public static final int MINIMUM_BUCKET = 1;
    public static final int MAXIMUM_BUCKET = 256;

    private final NodeRecord homeNode;
    private final LivenessManager livenessManager;
    private final Map<Integer, KBucket> buckets = new HashMap<>();
    private final Clock clock = Clock.systemUTC();

    public NodeTable(final NodeRecord homeNode, final LivenessChecker livenessChecker) {
        this.homeNode = homeNode;
        this.livenessManager = new LivenessManager(this.clock, livenessChecker);
    }

    public synchronized Stream<NodeRecord> getLiveNodeRecords(int distance) {
        if (distance == 0) {
            return Stream.of(homeNode);
        }
        return streamFromBucket(distance, bucket -> bucket.getLiveNodes().stream());
    }


    public Stream<NodeRecord> streamClosestNodes(Bytes nodeId) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new KBucketsIterator(this, this.homeNode.getNodeId(), nodeId), Spliterator.ORDERED), false);
    }

    public synchronized void offer(NodeRecord node) {
        final int distance = Functions.logDistance(this.homeNode.getNodeId(), node.getNodeId());
        if (distance > MAXIMUM_BUCKET) return;
        getOrCreateBucket(distance).ifPresent(bucket -> bucket.offer(node));
    }


    public synchronized void addNode(NodeRecord node) {
        final int distance = Functions.logDistance(this.homeNode.getNodeId(), node.getNodeId());
        getOrCreateBucket(distance).ifPresent(bucket -> bucket.add(node));
    }

    public synchronized Optional<NodeRecord> getNode(final Bytes nodeId) {
        return getBucket(Functions.logDistance(this.homeNode.getNodeId(), nodeId)).flatMap(bucket -> bucket.getNode(nodeId));
    }

    public synchronized boolean containsNode(final Bytes nodeId) {
        return getNode(nodeId).isPresent();
    }


    /**
     * Performs maintenance on the least recently touch bucket (excluding any empty buckets).
     */
    public synchronized void performMaintenance() {
        buckets.values().stream()
                .filter(bucket -> !bucket.isEmpty())
                .min(Comparator.comparing(KBucket::getLastMaintenanceTime))
                .ifPresent(KBucket::performMaintenance);
    }


    /**
     * Extracts a {@link Stream} from a {@link KBucket} at a given distance, ensuring that all
     * accesses to the KBucket are completed prior to the method returning.
     *
     * <p>Specifically this method avoids using {@code getBucket(distance).stream().flatMap(mapper)}
     * as the mapper is then called after the method returns. That leads to thread safety issues.
     *
     * @param distance the bucket distance
     * @param mapper   the function to convert the KBucket to a Stream of objects
     * @param <T>      the type of object in the returned Stream
     * @return the result of applying mapper to the bucket at the requested distance, or an empty
     * stream if there is no bucket at that distance.
     */
    private <T> Stream<T> streamFromBucket(int distance, final Function<KBucket, Stream<T>> mapper) {
        final Optional<KBucket> bucket = getBucket(distance);
        if (bucket.isPresent()) {
            return mapper.apply(bucket.get());
        } else {
            return Stream.empty();
        }
    }

    private Optional<KBucket> getOrCreateBucket(final int distance) {
        if (distance > MAXIMUM_BUCKET || distance < MINIMUM_BUCKET) {
            // Distance too great, ignore.
            return Optional.empty();
        }
        return Optional.of(
                buckets.computeIfAbsent(distance, __ -> new KBucket(livenessManager, clock)));
    }

    private Optional<KBucket> getBucket(final int distance) {
        return Optional.ofNullable(buckets.get(distance));
    }

  /*

  public synchronized BucketStats getStats() {
    final BucketStats stats = new BucketStats();
    buckets.forEach((distance, bucket) -> bucket.updateStats(distance, stats));
    return stats;
  }

*/

}
