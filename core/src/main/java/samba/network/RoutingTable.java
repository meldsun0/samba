package samba.network;


import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.apache.tuweni.units.bigints.UInt64;


public interface RoutingTable {

    public void evictNode(UInt64 nodeId);

    public void updateRadius(UInt64 nodeId, UInt64 radius);

    public UInt64 getRadius(UInt64 nodeId);

    boolean isKnown(NodeRecord nodeRecord);
}
