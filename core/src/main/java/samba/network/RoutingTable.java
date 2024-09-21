package samba.network;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.node.NodeId;

public interface RoutingTable {


    public void updateRoutingTable();

    public void evictNode(UInt64 nodeId);

    void updateRadius(NodeId nodeId, int radius);
}
