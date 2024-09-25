package samba.network.history;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.node.NodeId;
import samba.network.RoutingTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryRoutingTable implements RoutingTable {


   private final Map<NodeId, UInt64>  radiusMap;

    public HistoryRoutingTable(){
        this.radiusMap = new ConcurrentHashMap<>();
    }

    @Override
    public void evictNode(UInt64 nodeId) {
        this.radiusMap.remove(nodeId);
    }

    @Override
    public void updateRadius(NodeId nodeId, UInt64 radius) {
        this.radiusMap.put(nodeId, radius);
    }
    
    @Override
    public UInt64 getRadius(NodeId nodeId){
        return this.radiusMap.get(nodeId);
    }
}
