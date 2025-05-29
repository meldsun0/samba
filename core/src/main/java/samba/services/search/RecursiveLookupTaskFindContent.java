/*
 * SPDX-License-Identifier: Apache-2.0
 */

package samba.services.search;

import samba.api.jsonrpc.results.FindContentResult;
import samba.domain.messages.requests.FindContent;
import samba.network.history.HistoryNetwork;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.hyperledger.besu.crypto.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveLookupTaskFindContent {

  private static final Logger LOG = LoggerFactory.getLogger(RecursiveLookupTaskFindContent.class);

  private static final int MAX_CONCURRENT_QUERIES = 3;
  private final HistoryNetwork historyNetwork;
  private final Bytes contentKey;
  private final Set<Bytes> queriedNodeIds = new HashSet<>();
  private int availableQuerySlots = MAX_CONCURRENT_QUERIES;
  private final CompletableFuture<Optional<FindContentResult>> future = new CompletableFuture<>();
  private final Set<NodeRecord> foundNodes = new HashSet<>();
  private Optional<FindContentResult> content = Optional.empty();
  private final int timeout;
  private final Set<NodeRecord> interestedNodes = new HashSet<>();
  private final Set<NodeRecord> excludedNodes = new HashSet<>();
  private Optional<NodeRecord> respondingNode;

  public RecursiveLookupTaskFindContent(
      final HistoryNetwork historyNetwork,
      final Bytes contentKey,
      final Bytes homeNodeId,
      final Set<NodeRecord> foundNodes,
      final int timeout) {
    this(historyNetwork, contentKey, homeNodeId, foundNodes, Set.of(), timeout);
  }

  public RecursiveLookupTaskFindContent(
      final HistoryNetwork historyNetwork,
      final Bytes contentKey,
      final Bytes homeNodeId,
      final Set<NodeRecord> foundNodes,
      final Set<NodeRecord> excludedNodes,
      final int timeout) {
    this.historyNetwork = historyNetwork;
    this.contentKey = contentKey;
    this.queriedNodeIds.add(homeNodeId);
    this.foundNodes.addAll(foundNodes);
    this.timeout = timeout;
    this.excludedNodes.addAll(excludedNodes);
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
      LOG.error("No nodes left to query");
      future.complete(content);
      return;
    }

    final List<NodeRecord> nodesToQuery =
        foundNodes.stream()
            .filter(record -> !queriedNodeIds.contains(record.getNodeId()))
            .limit(availableQuerySlots)
            .collect(Collectors.toList());

    if (nodesToQuery.isEmpty()) {
      future.complete(content);
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
                if (future.isDone()) {
                  return;
                }
                availableQuerySlots++;
                if (result.isEmpty()) {
                  LOG.debug("Node {} returned empty result", peer.getNodeId());
                } else {
                  FindContentResult contentResult = result.get();
                  if (contentResult.getContent() != null && !excludedNodes.contains(peer)) {
                    this.respondingNode = Optional.of(peer);
                    content = Optional.of(contentResult);
                    future.complete(content);
                    return;
                  }
                  // Add nodes to foundNodes and continue searching
                  foundNodes.addAll(
                      Optional.ofNullable(contentResult.getEnrs())
                          .orElseGet(Collections::emptyList)
                          .stream()
                          .map(historyNetwork::nodeRecordFromEnr)
                          .flatMap(Optional::stream)
                          .filter(node -> !queriedNodeIds.contains(node.getNodeId()))
                          .collect(Collectors.toSet()));
                  UInt256 peerDistance =
                      UInt256.fromBytes(peer.getNodeId().xor(Hash.sha256(contentKey)));
                  if (peerDistance.lessOrEqualThan(historyNetwork.getRadiusFromNode(peer)))
                    interestedNodes.add(peer);
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

  public Set<NodeRecord> getInterestedNodes() {
    return interestedNodes;
  }

  public Optional<NodeRecord> getRespondingNode() {
    return respondingNode;
  }
}
