package samba.network;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.node.NodeId;

public interface RoutingTable {

    public void evictNode(UInt64 nodeId);

    public void updateRadius(NodeId nodeId, UInt64 radius);

    public UInt64 getRadius(NodeId nodeId);
}
