package samba.network.history;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.RoutingTable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//Nodes can be inserted into the routing table into the appropriate bucket, ensuring that buckets do not end up containing duplicate records.
public class HistoryRoutingTable implements RoutingTable{

   private final Map<Bytes, UInt256> radiusMap;

   //  private KBuckets dht; //distance -> [nodes]
   //gossip ?

    public HistoryRoutingTable(){
        this.radiusMap = new ConcurrentHashMap<Bytes, UInt256> ();
    }

    @Override
    public void removeRadius(Bytes nodeId) {
        this.radiusMap.remove(nodeId);
        //TODO remove from dht and gossip?
    }

    /**
     *
     * @param nodeId
     * @param radius This value is a 256 bit integer and represents the data that a node is "interested" in.
     */
    @Override
    public void updateRadius(Bytes nodeId, UInt256 radius) {
        this.radiusMap.put(nodeId, radius);
    }
    
    @Override
    public UInt256 getRadius(Bytes nodeId){
        return this.radiusMap.get(nodeId);
    }

    @Override
    public boolean isKnown(Bytes nodeId) {
        return false;
        //TODO  use a DHT
       // return this.dht.containsNode(nodeRecord.getNodeId());
    }

    @Override
    public Optional<NodeRecord> findNode(Bytes nodeId) {
        return Optional.empty();
    }

    @Override
    public Object updateNode(NodeRecord nodeRecord) {
        return null;
    }

}
