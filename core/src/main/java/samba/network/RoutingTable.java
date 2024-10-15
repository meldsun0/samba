package samba.network;


import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public interface RoutingTable {

    void evictNode(UInt64 nodeId);

    void updateRadius(UInt64 nodeId, UInt256 radius);

    UInt256 getRadius(UInt64 nodeId);

    boolean isKnown(UInt64 nodeId);



}
