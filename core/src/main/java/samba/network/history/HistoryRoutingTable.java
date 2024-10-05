package samba.network.history;

import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.liveness.LivenessChecker;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.storage.KBuckets;
import org.ethereum.beacon.discovery.storage.LocalNodeRecordStore;
import samba.domain.node.NodeId;
import samba.network.RoutingTable;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//distance -> [nodes]
public class HistoryRoutingTable implements RoutingTable {

   private final Map<NodeId, UInt64>  radiusMap;
   private KBuckets dht;

    public HistoryRoutingTable(final Clock clock,
                               final LocalNodeRecordStore localNodeRecordStore,
                               final LivenessChecker livenessChecker){
        dht = new KBuckets(clock, localNodeRecordStore, livenessChecker);
        this.radiusMap = new ConcurrentHashMap<>();
    }

    @Override
    public void evictNode(UInt64 nodeId) {
        this.radiusMap.remove(nodeId);
    }

    /**
     *
     * @param nodeId
     * @param radius This value is a 256 bit integer and represents the data that a node is "interested" in.
     */
    @Override
    public void updateRadius(NodeId nodeId, UInt64 radius) {
        this.radiusMap.put(nodeId, radius);
    }
    
    @Override
    public UInt64 getRadius(NodeId nodeId){
        return this.radiusMap.get(nodeId);
    }

    @Override
    public boolean isKnown(NodeRecord nodeRecord) {
        //TODO
        return false;
    }

}
