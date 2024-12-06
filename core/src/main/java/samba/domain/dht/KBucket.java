/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.domain.dht;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

class KBucket {

  private static final int K = 16;
  private long lastMaintenanceTime = 0;

  private final LivenessManager livenessManager;
  private final Clock clock;

  private final List<BucketEntry> nodes = new ArrayList<>();
  private Optional<BucketEntry> pendingNode = Optional.empty();

  public KBucket(final LivenessManager livenessManager, final Clock clock) {
    this.livenessManager = livenessManager;
    this.clock = clock;
  }

  public void updateStats(final int distance, final BucketStats stats) {
    stats.setBucketStat(distance, (int) streamLiveEntries().count(), nodes.size());
  }

  public List<NodeRecord> getAllNodes() {
    return nodes.stream().map(BucketEntry::getNode).collect(Collectors.toList());
  }

  public List<NodeRecord> getLiveNodes() {
    return streamLiveEntries().map(BucketEntry::getNode).collect(Collectors.toList());
  }

  private Stream<BucketEntry> streamLiveEntries() {
    return nodes.stream().takeWhile(BucketEntry::isLive);
  }

  public Optional<NodeRecord> getPendingNode() {
    return pendingNode.map(BucketEntry::getNode);
  }

  // TODO validate when trigger  newNode.checkLiveness(currentTime);
  public void addOrUpdate(final NodeRecord node) {
    performMaintenance();
    getEntry(node)
        .ifPresentOrElse(
            existingEntry -> {
              updateExistingEntry(existingEntry, node);
            },
            () -> {
              if (livenessManager.isABadPeer(node)) return;
              addNewNode(node);
            });
  }

  // TODO validate if it should be removed from the liveness checker.
  public void remove(final NodeRecord node) {
    getEntry(node)
        .ifPresentOrElse(
            existingEntry -> {
              nodes.remove(existingEntry);
              this.promotePendingNode();
            },
            () -> {
              if (isPendingNode(node)) {
                pendingNode = Optional.empty();
              }
            });
  }

  /**
   * Performs any pending maintenance on the bucket.
   *
   * <p>If the pending node has not been pinged recently, schedule a ping for it.
   *
   * <p>If the pending node has not responded to the last ping within a reasonable time, remove it.
   *
   * <p>If the last node in the bucket has not been pinged recently, schedule a ping for it.
   *
   * <p>If the last node in the bucket has not responded to the last liveness check within a
   * reasonable time:
   *
   * <p>a. remove it from the bucket.
   *
   * <p>b. if there is a pending node, insert it into the bucket (at appropriate position based on
   * when it was last confirmed as live)
   */
  public void performMaintenance() {
    final long currentTime = clock.millis();
    lastMaintenanceTime = currentTime;
    performPendingNodeMaintenance();

    if (nodes.isEmpty()) return;

    final BucketEntry lastNode = getLastNode();
    if (lastNode.hasFailedLivenessCheck(currentTime)) {
      nodes.remove(lastNode);
      this.promotePendingNode();
    } else {
      lastNode.checkLiveness(currentTime);
    }
  }

  private void promotePendingNode() {
    pendingNode.ifPresent(
        pendingEntry -> {
          nodes.addFirst(pendingEntry);
          pendingNode = Optional.empty();
        });
  }

  public long getLastMaintenanceTime() {
    return lastMaintenanceTime;
  }

  public Optional<NodeRecord> getNode(final Bytes targetNodeId) {
    return getEntry(targetNodeId).map(BucketEntry::getNode);
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  private boolean isPendingNode(NodeRecord node) {
    return pendingNode.isPresent() && pendingNode.get().getNodeId().equals(node.getNodeId());
  }

  private void addNewNode(NodeRecord newNode) {
    long currentTime = clock.millis();
    if (isFull()) {
      if (pendingNode.isEmpty()) {
        pendingNode = Optional.of(new BucketEntry(livenessManager, newNode, currentTime));
      } else {
        if (isPendingNode(newNode)) {
          pendingNode = Optional.of(new BucketEntry(livenessManager, newNode, currentTime));
        }
      }
    } else {
      nodes.addFirst(new BucketEntry(livenessManager, newNode, currentTime));
    }
  }

  private void updateExistingEntry(BucketEntry existingEntry, final NodeRecord newRecord) {
    nodes.remove(existingEntry);
    if (itsNewEntry(existingEntry, newRecord)) {
      nodes.addFirst(new BucketEntry(livenessManager, newRecord, clock.millis()));
    } else {
      nodes.addFirst(new BucketEntry(livenessManager, existingEntry.getNode(), clock.millis()));
    }
  }

  private static boolean itsNewEntry(BucketEntry existingEntry, NodeRecord newRecord) {
    return !(existingEntry.getNode().getSeq().compareTo(newRecord.getSeq()) >= 0);
  }

  private void updatePendingNodeTime() {
    pendingNode = Optional.of(pendingNode.get().withLastConfirmedTime(clock.millis()));
  }

  private void performPendingNodeMaintenance() {
    pendingNode.ifPresent(
        pendingEntry -> {
          final long currentTime = clock.millis();
          if (pendingEntry.hasFailedLivenessCheck(currentTime)) {
            pendingNode = Optional.empty();
          } else {
            pendingEntry.checkLiveness(currentTime);
          }
        });
  }

  private BucketEntry getLastNode() {
    return nodes.get(nodes.size() - 1);
  }

  private boolean isFull() {
    return nodes.size() >= K;
  }

  private Optional<BucketEntry> getEntry(final NodeRecord nodeRecord) {
    return getEntry(nodeRecord.getNodeId());
  }

  private Optional<BucketEntry> getEntry(final Bytes nodeId) {
    return nodes.stream().filter(node -> node.getNodeId().equals(nodeId)).findAny();
  }
}
