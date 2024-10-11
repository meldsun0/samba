package samba.network;


import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public interface RoutingTable {

    public void evictNode(UInt64 nodeId);

    public void updateRadius(UInt64 nodeId, UInt256 radius);

    public UInt256 getRadius(UInt64 nodeId);

    boolean isKnown(NodeRecord nodeRecord);
}
