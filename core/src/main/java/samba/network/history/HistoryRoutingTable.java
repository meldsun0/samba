package samba.network.history;

import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.liveness.LivenessChecker;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.storage.KBuckets;
import org.ethereum.beacon.discovery.storage.LocalNodeRecordStore;
import org.ethereum.beacon.discovery.storage.NewAddressHandler;
import org.ethereum.beacon.discovery.storage.NodeRecordListener;
import org.ethereum.beacon.discovery.util.Functions;
import samba.domain.node.NodeId;
import samba.network.RoutingTable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryRoutingTable implements RoutingTable {

   private final Map<UInt64, UInt64> radiusMap;
 //  private KBuckets dht; //distance -> [nodes]
   //gossip ?

    public HistoryRoutingTable(){
        this.radiusMap = new ConcurrentHashMap<UInt64, UInt64> ();
    }

    @Override
    public void evictNode(UInt64 nodeId) {
        this.radiusMap.remove(nodeId);
        //TODO remove from dht and gossip?
    }

    /**
     *
     * @param nodeId
     * @param radius This value is a 256 bit integer and represents the data that a node is "interested" in.
     */
    @Override
    public void updateRadius(UInt64 nodeId, UInt64 radius) {
        this.radiusMap.put(nodeId, radius);
    }
    
    @Override
    public UInt64 getRadius(UInt64 nodeId){
        return this.radiusMap.get(nodeId);
    }

    @Override
    public boolean isKnown(NodeRecord nodeRecord) {
        return false;
        //TODO  use a DHT
       // return this.dht.containsNode(nodeRecord.getNodeId());
    }

}
