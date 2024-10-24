package samba.network;


import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import java.util.Optional;

public interface RoutingTable {

    void removeRadius(Bytes nodeId);

    void updateRadius(Bytes nodeId, UInt256 radius);

    UInt256 getRadius(Bytes nodeId);

    boolean isKnown(Bytes nodeId);


    Optional<NodeRecord> findNode(Bytes nodeId);


    void addNode(NodeRecord nodeRecord);
}
