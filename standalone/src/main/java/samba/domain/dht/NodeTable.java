package samba.domain.dht;

import java.time.Clock;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.util.Functions;

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

  public synchronized Stream<NodeRecord> getLiveNodeRecords(final int distance) {
    if (distance == 0) return Stream.of(homeNode);
    return streamFromBucket(distance, bucket -> bucket.getLiveNodes().stream());
  }

  public synchronized void addNode(NodeRecord node) {
    final int distance = Functions.logDistance(this.homeNode.getNodeId(), node.getNodeId());
    if (distance > MAXIMUM_BUCKET) return;
    getOrCreateBucket(distance).ifPresent(bucket -> bucket.addOrUpdate(node));
  }

  public synchronized Stream<NodeRecord> streamClosestNodes(final Bytes nodeId) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            new KBucketsIterator(this, this.homeNode.getNodeId(), nodeId), Spliterator.ORDERED),
        false);
  }

  public synchronized Optional<NodeRecord> getNode(final Bytes nodeId) {
    final int distance = Functions.logDistance(this.homeNode.getNodeId(), nodeId);
    return getBucket(distance).flatMap(bucket -> bucket.getNode(nodeId));
  }

  public synchronized void removeNode(final NodeRecord nodeRecord) {
    final int distance = Functions.logDistance(this.homeNode.getNodeId(), nodeRecord.getNodeId());
    getBucket(distance).ifPresent(bucket -> bucket.remove(nodeRecord));
  }

  public synchronized void performMaintenanceOnOldestBucket() {
    buckets.values().stream()
        .filter(bucket -> !bucket.isEmpty())
        .min(Comparator.comparing(KBucket::getLastMaintenanceTime))
        .ifPresent(KBucket::performMaintenance);
  }

  public synchronized BucketStats getStats() {
    final BucketStats stats = new BucketStats();
    buckets.forEach((distance, bucket) -> bucket.updateStats(distance, stats));
    return stats;
  }

  private <T> Stream<T> streamFromBucket(
      final int distance, final Function<KBucket, Stream<T>> mapper) {
    final Optional<KBucket> bucket = getBucket(distance);
    if (bucket.isPresent()) return mapper.apply(bucket.get());
    return Stream.empty();
  }

  private Optional<KBucket> getOrCreateBucket(final int distance) {
    if (isOutOfDistance(distance)) return Optional.empty();
    return Optional.of(
        buckets.computeIfAbsent(distance, __ -> new KBucket(livenessManager, clock)));
  }

  private static boolean isOutOfDistance(final int distance) {
    return distance > MAXIMUM_BUCKET || distance < MINIMUM_BUCKET;
  }

  private Optional<KBucket> getBucket(final int distance) {
    return Optional.ofNullable(buckets.get(distance));
  }

  public boolean isNodeIgnored(NodeRecord nodeRecord) {
    return this.livenessManager.isABadPeer(nodeRecord);
  }
}
