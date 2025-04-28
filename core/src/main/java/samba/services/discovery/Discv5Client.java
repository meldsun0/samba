package samba.services.discovery;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Discv5Client {

  CompletableFuture<Bytes> sendDiscv5Message(NodeRecord nodeRecord, Bytes protocol, Bytes request);

  SafeFuture<Collection<NodeRecord>> streamLiveNodes();

  Optional<Bytes> getNodeId();

  NodeRecord getHomeNodeRecord();

  Optional<String> getEnr();

  UInt64 getEnrSeq();

  void updateCustomENRField(final String fieldName, final Bytes value);

  boolean updateEnrSocket(InetSocketAddress socketAddress, boolean isTCP);

  Optional<String> lookupEnr(final UInt256 nodeId);

  CompletableFuture<Void> ping(NodeRecord nodeRecord);

  CompletableFuture<Collection<NodeRecord>> findNodes(
      final NodeRecord nodeRecord, final List<Integer> distances);
}
