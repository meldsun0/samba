package samba.network.history;

import samba.domain.content.ContentKey;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.services.FindContentResult;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface HistoryJsonRpcRequests {

  SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message);

  SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes findNodes);

  SafeFuture<Optional<FindContentResult>> findContent(
      NodeRecord nodeRecord, FindContent findContent);

  void addEnr(String enr);

  Optional<String> getEnr(String nodeId);

  boolean deleteEnr(String nodeId);

  boolean store(Bytes contentKey, Bytes contentValue);

  Optional<String> getLocalContent(ContentKey contentKey);

  SafeFuture<Optional<Bytes>> offer(NodeRecord nodeRecord, List<Bytes> content, Offer offer);

  Optional<String> lookupEnr(final UInt256 nodeId);
}
