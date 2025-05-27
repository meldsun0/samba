/*
 * SPDX-License-Identifier: Apache-2.0
 */

package samba.services.search;

import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import samba.network.history.HistoryNetwork;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveLookupTaskFindNodes {

  private static final Logger LOG = LoggerFactory.getLogger(RecursiveLookupTaskFindNodes.class);

  private static final int MAX_CONCURRENT_QUERIES = 3;
  private static final int MAX_NODE_LIST_COUNT = 16;
  private final HistoryNetwork historyNetwork;
  private int availableQuerySlots = MAX_CONCURRENT_QUERIES;
  private final CompletableFuture<Optional<RecursiveFindNodesResult>> future =
      new CompletableFuture<>();
  private final int timeout;
  private final Bytes targetNodeId;
  private final Set<Bytes> queriedNodeIds = new HashSet<>();
  private final SortedSet<NodeRecord> foundNodes;
  private final Set<NodeRecord> excludedNodes = new HashSet<>();

  public RecursiveLookupTaskFindNodes(
      final HistoryNetwork historyNetwork,
      final Bytes targetNodeId,
      final Bytes homeNodeId,
      final Set<NodeRecord> foundNodes,
      final Set<NodeRecord> excludedNodes,
      final int timeout) {
    this.historyNetwork = historyNetwork;
    this.targetNodeId = targetNodeId;
    this.queriedNodeIds.add(homeNodeId);
    this.foundNodes =
        new TreeSet<>(
            Comparator.comparing(
                foundNode -> UInt256.fromBytes(foundNode.getNodeId().xor(targetNodeId))));
    this.foundNodes.addAll(foundNodes);
    this.excludedNodes.addAll(excludedNodes);
    this.timeout = timeout;
  }

  public CompletableFuture<Optional<RecursiveFindNodesResult>> execute() {
    sendRequests();
    return future.completeOnTimeout(
        foundNodes.isEmpty()
            ? Optional.empty()
            : Optional.of(
                new RecursiveFindNodesResult(
                    foundNodes.stream()
                        .limit(MAX_NODE_LIST_COUNT)
                        .map(nodeRecord -> nodeRecord.getNodeId().toHexString())
                        .toList())),
        timeout,
        java.util.concurrent.TimeUnit.SECONDS);
  }

  private synchronized void sendRequests() {
    if (availableQuerySlots == 0 || future.isDone()) {
      return;
    }

    final List<NodeRecord> nodesToQuery =
        foundNodes.stream()
            .filter(record -> !queriedNodeIds.contains(record.getNodeId()))
            .limit(availableQuerySlots)
            .collect(Collectors.toList());

    final boolean closestCondition =
        foundNodes.stream()
                .limit(MAX_NODE_LIST_COUNT)
                .filter(record -> !excludedNodes.contains(record))
                .filter(
                    record ->
                        UInt256.fromBytes(record.getNodeId().xor(targetNodeId))
                                    .toBigInteger()
                                    .bitLength()
                                - 1
                            <= 1)
                .count()
            == MAX_NODE_LIST_COUNT;

    if (nodesToQuery.isEmpty() || closestCondition) {
      future.complete(
          foundNodes.isEmpty()
              ? Optional.empty()
              : Optional.of(
                  new RecursiveFindNodesResult(
                      foundNodes.stream()
                          .filter(record -> !excludedNodes.contains(record))
                          .limit(MAX_NODE_LIST_COUNT)
                          .map(nodeRecord -> nodeRecord.asEnr())
                          .toList())));
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
    Integer logDistance =
        UInt256.fromBytes(peer.getNodeId().xor(targetNodeId)).toBigInteger().bitLength() - 1;
    Set<Integer> logDistances = new HashSet<>();
    logDistances.add(logDistance);
    if (!peer.getNodeId().equals(targetNodeId)) {
      logDistances.add(logDistance + 1);
      logDistances.add(logDistance - 1);
    }
    historyNetwork
        .findNodes(peer, new FindNodes(logDistances))
        .thenAccept(
            result -> {
              synchronized (this) {
                if (future.isDone()) {
                  return;
                }
                availableQuerySlots++;
                if (result.isEmpty()) {
                  LOG.debug("Node {} returned empty result", peer.getNodeId());
                } else {
                  Nodes nodesResult = result.get();
                  // Add nodes to foundNodes and continue searching
                  foundNodes.addAll(
                      nodesResult.getEnrList().stream()
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
