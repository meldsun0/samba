package samba.services.discovery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.libp2p.core.multiformats.Multiaddr;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Discv5Client {

  CompletableFuture<Bytes> sendDisv5Message(NodeRecord nodeRecord, Bytes protocol, Bytes request);

  SafeFuture<Collection<NodeRecord>> streamLiveNodes();

  Optional<Bytes> getNodeId();

  NodeRecord getHomeNodeRecord();

  Optional<String> getEnr();

  UInt64 getEnrSeq();

  CompletableFuture<Collection<NodeRecord>> sendDiscv5FindNodes(
      NodeRecord nodeRecord, List<Integer> distances);

  void updateCustomENRField(final String fieldName, final Bytes value);

  NodeRecord updateNodeRecordSocket(Multiaddr multiaddr);

  Optional<String> lookupEnr(final UInt256 nodeId);
}
