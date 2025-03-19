/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.domain.dht;

import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.database.ExpirationSet;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public class LivenessManager {
  private static final Logger LOG = LogManager.getLogger();

  static final int MAX_CONCURRENT_PINGS = 3;
  static final int MAX_QUEUE_SIZE = 1000;
  static final int MAX_IGNORE_SET_SIZE = 1000;
  static final Duration IGNORE_DURATION = Duration.ofSeconds(30);

  private final Set<NodeRecord> activePings = new HashSet<>();
  private final Set<NodeRecord> queuedPings = new LinkedHashSet<>();
  private final ExpirationSet<Bytes> ignoredNodes; // TODO make thread-safe
  private final LivenessChecker livenessChecker;

  public LivenessManager(final Clock clock, final LivenessChecker livenessChecker) {
    this.ignoredNodes = new ExpirationSet<>(IGNORE_DURATION, clock, MAX_IGNORE_SET_SIZE);
    this.livenessChecker = livenessChecker;
  }

  /**
   * Adds the specified node to the queue of nodes to perform a liveness check on.
   *
   * @param node the node to check liveness
   */
  public synchronized void checkLiveness(NodeRecord node) {
    if (activePings.contains(node) || isABadPeer(node)) {
      // Already checking node or node should be ignored
      // Note: We don't need to check queuedPings because it's a set so the add just does nothing
      // and if we have capacity to ping immediately the queue must be empty.
      return;
    }
    if (activePings.size() < MAX_CONCURRENT_PINGS) {
      sendPing(node);
    } else if (queuedPings.size() < MAX_QUEUE_SIZE) {
      queuedPings.add(node);
    }
  }

  private void sendPing(final NodeRecord node) {
    queuedPings.remove(node);
    activePings.add(node);
    livenessChecker
        .checkLiveness(node)
        .whenComplete(
            (__, error) -> {
              if (error != null) {
                LOG.trace("Liveness check failed for node {}", node, error);
              }
              synchronized (this) {
                if (error != null) {
                  ignoredNodes.add(node.getNodeId());
                }
                activePings.remove(node);
                // Ping the next node in the queue if any.
                queuedPings.stream().findFirst().ifPresent(this::sendPing);
              }
            });
  }

  public synchronized boolean isABadPeer(final NodeRecord nodeRecord) {
    return ignoredNodes.contains(nodeRecord.getNodeId());
  }

  public synchronized void ignoreNode(final NodeRecord nodeRecord) {
    ignoredNodes.add(nodeRecord.getNodeId());
  }
}
