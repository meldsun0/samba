package samba.network.history.api;

import samba.api.jsonrpc.results.FindContentResult;
import samba.domain.content.ContentKey;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface HistoryNetworkInternalAPI {

  SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message);

  SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes findNodes);

  SafeFuture<Optional<FindContentResult>> findContent(
      NodeRecord nodeRecord, FindContent findContent);

  Optional<String> getLocalContent(ContentKey contentKey);

  SafeFuture<Optional<Bytes>> offer(NodeRecord nodeRecord, List<Bytes> content, Offer offer);

  Optional<String> lookupEnr(final UInt256 nodeId);

  Set<NodeRecord> getFoundNodes(ContentKey contentKey, int count, boolean inRadius);

  void gossip(final Set<NodeRecord> nodes, final Bytes key, final Bytes content);

  int getMaxGossipCount();

  boolean store(Bytes contentKey, Bytes contentValue);

  boolean addEnr(String enr);

  boolean deleteEnr(String nodeId);

  Optional<String> getEnr(String nodeId);
}
