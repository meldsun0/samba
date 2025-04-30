package samba.network.history.routingtable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface RoutingTable {

  void removeRadius(Bytes nodeId);

  void updateRadius(Bytes nodeId, UInt256 radius);

  UInt256 getRadius(Bytes nodeId);

  Optional<NodeRecord> findNode(Bytes nodeId);

  Optional<NodeRecord> findClosestNodeToKey(Bytes key);

  Set<NodeRecord> findClosestNodesToKey(Bytes key, int count, boolean inRadius);

  Stream<NodeRecord> getNodes(final int distance);

  void addOrUpdateNode(NodeRecord nodeRecord);

  void removeNode(NodeRecord nodeRecord);

  int getActiveNodes();

  boolean isNodeConnected(Bytes nodeId);

  boolean isNodeIgnored(NodeRecord nodeRecord);
}
