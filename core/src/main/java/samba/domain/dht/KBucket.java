/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.domain.dht;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;


import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //TODO validate when trigger  newNode.checkLiveness(currentTime);
    public void addOrUpdate(final NodeRecord node) {
        getEntry(node)
                .ifPresentOrElse(
                        existingEntry -> {
                            updateExistingEntry(existingEntry, node);
                            performMaintenance();
                        },
                        () -> {
                            if (livenessManager.isABadPeer(node)) return;
                            if (isPendingNode(node)) updatePendingNodeTime();
                            performMaintenance();
                            addNewNode(node);
                        });
    }

    //TODO validate if it should be removed from the liveness checker.
    public void remove(final NodeRecord node){
        getEntry(node).ifPresent( existingEntry -> {
                          if (isPendingNode(node)){
                              pendingNode = Optional.empty();
                          }else{
                              nodes.remove(existingEntry);
                          }
                });
        performMaintenance();
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
            pendingNode.ifPresent(
                    pendingEntry -> {
                        nodes.addFirst(pendingEntry);
                        pendingNode = Optional.empty();
                    });
        } else {
            lastNode.checkLiveness(currentTime);
        }
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

    private void addNewNode(NodeRecord node) {
        if (isFull()) {
            if (pendingNode.isEmpty()) {
                pendingNode = Optional.of(new BucketEntry(livenessManager, node, clock.millis()));
            }
        } else {
            nodes.addFirst(new BucketEntry(livenessManager, node, clock.millis()));
        }
    }

    private void updateExistingEntry(BucketEntry existingEntry, final NodeRecord newRecord) {
        nodes.remove(existingEntry);
        nodes.addFirst(new BucketEntry(livenessManager, newRecord, clock.millis()));
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