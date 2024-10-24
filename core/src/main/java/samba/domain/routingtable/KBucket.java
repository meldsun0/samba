/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.domain.routingtable;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;


import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class KBucket {

    static final int K = 16;

    private final LivenessManager livenessManager;
    private final Clock clock;

    /**
     * The nodes actually in the bucket, ordered by time they were last confirmed as live.
     *
     * <p>Thus the live nodes are at the start of the bucket with not yet confirmed nodes at the end,
     * and the last node in the list is the node least recently confirmed as live that should be the
     * next to check.
     */
    private final List<BucketEntry> nodes = new ArrayList<>();
    private Optional<BucketEntry> pendingNode = Optional.empty();
    private long lastMaintenanceTime = 0;

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

    //actualuliza el node existente o agrega uno nuevo en caso de q haya lugar y no sea un ignorado.
    public void offer(final NodeRecord node) {
        performMaintenance();
        getEntry(node)
                .ifPresentOrElse(
                    existing -> updateExistingRecord(existing, node), () -> offerNewNode(node));
    }



    private void offerNewNode(final NodeRecord node) {
        if (livenessManager.isABadPeer(node)) {
            return;
        }
        if (isFull()) {
            getLastNode().checkLiveness(clock.millis());
            if (pendingNode.isEmpty()) {
                livenessManager.checkLiveness(node);
            }
        } else {
            final BucketEntry newEntry = new BucketEntry(livenessManager, node);
            nodes.add(newEntry);
            newEntry.checkLiveness(clock.millis());
        }
    }


    public void add(final NodeRecord node) {
        getEntry(node)
                .ifPresentOrElse(
                        existingEntry -> {
                            updateExistingEntry(existingEntry, node);
                            performMaintenance();
                        },
                        () -> {



                            if (isPendingNode(node)) updatePendingNodeTime();
                            performMaintenance();
                            addNewNode(node);


                        });
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

    private void updateExistingEntry(BucketEntry existingEntry, final NodeRecord newRecord ) {
       // if (existingEntry.getNode().getSeq().compareTo(newRecord.getSeq()) >= 0) return;
        nodes.remove(existingEntry);
        nodes.addFirst(new BucketEntry(livenessManager, newRecord, clock.millis()));
        // newEntry.checkLiveness(clock.millis());
    }


    private void updatePendingNodeTime() {
        pendingNode = Optional.of(pendingNode.get().withLastConfirmedTime(clock.millis()));
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

        if (nodes.isEmpty()) {
            return;
        }
        final BucketEntry lastNode = getLastNode();
        if (lastNode.hasFailedLivenessCheck(currentTime)) {
            nodes.remove(lastNode);
            pendingNode.ifPresent(
                    pendingEntry -> {
                        nodes.add(0, pendingEntry);
                        pendingNode = Optional.empty();
                    });
        } else {
            lastNode.checkLiveness(currentTime);
        }
    }

    public long getLastMaintenanceTime() {
        return lastMaintenanceTime;
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

    public Optional<NodeRecord> getNode(final Bytes targetNodeId) {
        return getEntry(targetNodeId).map(BucketEntry::getNode);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}
