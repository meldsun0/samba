package samba.network;


import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import java.util.Optional;
import java.util.stream.Stream;

public interface RoutingTable {

    void removeRadius(Bytes nodeId);

    void updateRadius(Bytes nodeId, UInt256 radius);

    UInt256 getRadius(Bytes nodeId);

    Optional<NodeRecord> findNode(Bytes nodeId);

    Stream<NodeRecord> getNodes(final int distance);

    void addOrUpdateNode(NodeRecord nodeRecord);

    void removeNode(NodeRecord nodeRecord);

    int getActiveNodes();

    boolean isNodeConnected(Bytes nodeId);

    boolean isNodeIgnored(NodeRecord nodeRecord);

}
