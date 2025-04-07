/*
 * SPDX-License-Identifier: Apache-2.0
 */

package samba.services.search;

import samba.domain.messages.requests.FindContent;
import samba.network.history.HistoryNetwork;
import samba.services.FindContentResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public class RecursiveLookupTaskFindContent {
  private static final Logger LOG = LogManager.getLogger();
  private static final int MAX_CONCURRENT_QUERIES = 3;
  private final HistoryNetwork historyNetwork;
  private final Bytes contentKey;
  private final Set<Bytes> queriedNodeIds = new HashSet<>();
  private int availableQuerySlots = MAX_CONCURRENT_QUERIES;
  private final CompletableFuture<Optional<FindContentResult>> future = new CompletableFuture<>();
  private final Set<NodeRecord> foundNodes = new HashSet<>();
  private Optional<FindContentResult> content = Optional.empty();
  private final int timeout;

  public RecursiveLookupTaskFindContent(
      final HistoryNetwork historyNetwork,
      final Bytes contentKey,
      final Bytes homeNodeId,
      final Set<NodeRecord> foundNodes,
      final int timeout) {
    this.historyNetwork = historyNetwork;
    this.contentKey = contentKey;
    this.queriedNodeIds.add(homeNodeId);
    this.foundNodes.addAll(foundNodes);
    this.timeout = timeout;
  }

  public CompletableFuture<Optional<FindContentResult>> execute() {
    sendRequests();
    return future.completeOnTimeout(
        Optional.empty(), timeout, java.util.concurrent.TimeUnit.SECONDS);
  }

  private synchronized void sendRequests() {
    if (availableQuerySlots == 0 || future.isDone()) {
      return;
    }
    if (content.isPresent()) {
      future.complete(content);
      return;
    }

    final List<NodeRecord> nodesToQuery =
        foundNodes.stream()
            .filter(record -> !queriedNodeIds.contains(record.getNodeId()))
            .limit(availableQuerySlots)
            .collect(Collectors.toList());

    if (nodesToQuery.isEmpty()) {
      future.completeExceptionally(new RuntimeException("No nodes left to query."));
      return;
    }

    queryPeers(nodesToQuery);
  }

  private void queryPeers(final List<NodeRecord> nodesToQuery) {
    nodesToQuery.stream().map(NodeRecord::getNodeId).forEach(queriedNodeIds::add);
    availableQuerySlots -= nodesToQuery.size();
    nodesToQuery.forEach(this::queryPeer);
  }

  private void queryPeer(final NodeRecord peer) {
    historyNetwork
        .findContent(peer, new FindContent(contentKey))
        .thenAccept(
            result -> {
              synchronized (this) {
                availableQuerySlots++;
                if (result.isEmpty()) {
                  LOG.debug("Node {} returned empty result", peer.getNodeId());
                } else {
                  FindContentResult contentResult = result.get();
                  if (contentResult.getContent() != null) {
                    content = Optional.of(contentResult);
                    future.complete(content);
                    return;
                  }
                  // Add nodes to foundNodes and continue searching
                  foundNodes.addAll(
                      contentResult.getEnrs().stream()
                          .map(historyNetwork::nodeRecordFromEnr)
                          .flatMap(Optional::stream)
                          .filter(node -> !queriedNodeIds.contains(node.getNodeId()))
                          .collect(Collectors.toSet()));
                }
                sendRequests();
              }
            })
        .exceptionally(
            error -> {
              synchronized (this) {
                availableQuerySlots++;
                LOG.debug("Failed to query node {}: {}", peer.getNodeId(), error);
                sendRequests();
              }
              return null;
            });
  }
}
