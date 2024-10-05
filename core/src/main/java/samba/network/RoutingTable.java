package samba.network;

import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.node.Node;
import samba.domain.node.NodeId;

import java.util.List;

public interface RoutingTable {

    public void evictNode(UInt64 nodeId);

    public void updateRadius(NodeId nodeId, UInt64 radius);

    public UInt64 getRadius(NodeId nodeId);

    boolean isIgnored(NodeRecord nodeRecord);

    boolean isKnown(NodeRecord nodeRecord);
}
