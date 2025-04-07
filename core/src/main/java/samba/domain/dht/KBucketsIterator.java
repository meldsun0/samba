package samba.domain.dht;

import static java.util.stream.Collectors.toCollection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.util.Functions;

public class KBucketsIterator implements Iterator<NodeRecord> {

  private final NodeTable buckets;
  private final Comparator<NodeRecord> distanceComparator;
  private int lowDistance;
  private int highDistance;

  private Iterator<NodeRecord> currentBatch = Collections.emptyIterator();

  public KBucketsIterator(
      final NodeTable buckets, final Bytes homeNodeId, final Bytes targetNodeId) {
    this.buckets = buckets;
    this.distanceComparator =
        Comparator.comparing(node -> Functions.distance(targetNodeId, node.getNodeId()));
    final int initialDistance = Functions.logDistance(homeNodeId, targetNodeId);
    lowDistance = initialDistance;
    highDistance = initialDistance;
  }

  @Override
  public boolean hasNext() {
    while (!currentBatch.hasNext() && hasMoreBucketsToScan()) {
      updateCurrentBatch();
      // Prepare for the next buckets
      lowDistance--;
      highDistance++;
    }
    return currentBatch.hasNext();
  }

  @Override
  public NodeRecord next() {
    return currentBatch.next();
  }

  private boolean hasMoreBucketsToScan() {
    return lowDistance > 0 || highDistance <= NodeTable.MAXIMUM_BUCKET;
  }

  private void updateCurrentBatch() {
    final Stream<NodeRecord> lowNodes =
        lowDistance > 0 ? buckets.getLiveNodeRecords(lowDistance) : Stream.empty();
    final Stream<NodeRecord> highNodes =
        highDistance > lowDistance && highDistance <= NodeTable.MAXIMUM_BUCKET
            ? buckets.getLiveNodeRecords(highDistance)
            : Stream.empty();
    currentBatch =
        Stream.concat(lowNodes, highNodes)
            .collect(toCollection(() -> new TreeSet<>(distanceComparator)))
            .iterator();
  }
}
