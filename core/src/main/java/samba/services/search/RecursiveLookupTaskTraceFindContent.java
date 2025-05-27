/*
 * SPDX-License-Identifier: Apache-2.0
 */

package samba.services.search;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.TraceGetContentResult;
import samba.api.jsonrpc.schemas.TraceResultMetadataObjectJson;
import samba.api.jsonrpc.schemas.TraceResultObjectJson;
import samba.api.jsonrpc.schemas.TraceResultResponseItemJson;
import samba.domain.messages.requests.FindContent;
import samba.network.history.HistoryNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class RecursiveLookupTaskTraceFindContent {

  private static final Logger LOG =
      LoggerFactory.getLogger(RecursiveLookupTaskTraceFindContent.class);
  private static final int MAX_CONCURRENT_QUERIES = 3;
  private final HistoryNetwork historyNetwork;
  private final Bytes contentKey;
  private final Set<Bytes> queriedNodeIds = new HashSet<>();
  private int availableQuerySlots = MAX_CONCURRENT_QUERIES;
  private final CompletableFuture<Optional<TraceGetContentResult>> future =
      new CompletableFuture<>();
  private final Set<NodeRecord> foundNodes = new HashSet<>();
  private Optional<TraceGetContentResult> content = Optional.empty();
  private final int timeout;

  private final UInt256 origin;
  private final UInt256 targetId;
  private UInt256 receivedFrom;
  private final long startedAtMs;
  private final Map<UInt256, TraceResultResponseItemJson> responses = new HashMap<>();
  private final Map<UInt256, TraceResultMetadataObjectJson> metadata = new HashMap<>();
  private final List<UInt256> cancelled = new ArrayList<>();
  private final Set<NodeRecord> interestedNodes = new HashSet<>();

  public RecursiveLookupTaskTraceFindContent(
      final HistoryNetwork historyNetwork,
      final Bytes contentKey,
      final Bytes homeNodeId,
      final Set<NodeRecord> foundNodes,
      final int timeout,
      final long startTime) {
    this.historyNetwork = historyNetwork;
    this.contentKey = contentKey;
    this.queriedNodeIds.add(homeNodeId);
    this.foundNodes.addAll(foundNodes);
    this.timeout = timeout;
    this.startedAtMs = startTime;
    this.origin = historyNetwork.getLocalNodeId();
    this.targetId = UInt256.fromBytes(Hash.sha256(contentKey));
  }

  public CompletableFuture<Optional<TraceGetContentResult>> execute() {
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
              long durationMs = System.currentTimeMillis() - startedAtMs;
              List<UInt256> respondedWith = new ArrayList<>();
              String enr = peer.asEnr();
              UInt256 peerNodeId = UInt256.fromBytes(peer.getNodeId());
              UInt256 distance = UInt256.fromBytes(peerNodeId.xor(targetId));
              metadata.put(peerNodeId, new TraceResultMetadataObjectJson(enr, distance));
              synchronized (this) {
                if (future.isDone()) {
                  cancelled.add(peerNodeId);
                  return;
                }
                availableQuerySlots++;
                if (result.isEmpty()) {
                  LOG.debug("Node {} returned empty result", peer.getNodeId());
                } else {
                  FindContentResult contentResult = result.get();
                  if (contentResult.getContent() != null) {
                    receivedFrom = peerNodeId;
                    responses.put(
                        peerNodeId, new TraceResultResponseItemJson(durationMs, respondedWith));
                    TraceResultObjectJson traceResult =
                        new TraceResultObjectJson(
                            origin,
                            targetId,
                            receivedFrom,
                            responses,
                            metadata,
                            startedAtMs,
                            cancelled);
                    content =
                        Optional.of(
                            new TraceGetContentResult(
                                contentResult.getContent(),
                                contentResult.getUtpTransfer(),
                                traceResult));
                    future.complete(content);
                    return;
                  }
                  // Add nodes to foundNodes and continue searching
                  respondedWith.addAll(
                      contentResult.getEnrs().stream()
                          .map(historyNetwork::nodeRecordFromEnr)
                          .flatMap(Optional::stream)
                          .map(NodeRecord::getNodeId)
                          .map(UInt256::fromBytes)
                          .collect(Collectors.toList()));
                  foundNodes.addAll(
                      contentResult.getEnrs().stream()
                          .map(historyNetwork::nodeRecordFromEnr)
                          .flatMap(Optional::stream)
                          .filter(node -> !queriedNodeIds.contains(node.getNodeId()))
                          .collect(Collectors.toSet()));
                  UInt256 peerDistance =
                      UInt256.fromBytes(peer.getNodeId().xor(Hash.sha256(contentKey)));
                  if (peerDistance.lessOrEqualThan(historyNetwork.getRadiusFromNode(peer)))
                    interestedNodes.add(peer);
                }
                responses.put(
                    peerNodeId, new TraceResultResponseItemJson(durationMs, respondedWith));
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
}
