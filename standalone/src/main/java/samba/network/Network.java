package samba.network;

import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.services.jsonrpc.methods.results.FindContentResult;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Network {

  SafeFuture<Optional<Pong>> ping(NodeRecord node);

  SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes findNodes);

  SafeFuture<Optional<FindContentResult>> findContent(
      NodeRecord nodeRecord, FindContent findContent);

  SafeFuture<Optional<Bytes>> offer(NodeRecord nodeRecord, List<Bytes> content, Offer offer);

  int getNumberOfConnectedPeers();

  boolean isNodeConnected(NodeRecord node);

  UInt256 getRadiusFromNode(NodeRecord node);
}
