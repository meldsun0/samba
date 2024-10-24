package samba.network.history;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import samba.domain.routingtable.LivenessChecker;
import samba.domain.routingtable.NodeTable;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.RoutingTable;


import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** KBuckets: Represent distances from the local node perspective. It's a "binary tree whose leaves are k-buckets. The distance from myself is 0.
 * The binary tree exactly match the number of bits (256) in the key-space. (We have spot for any given distinction).
 * The greater the distance the greater the space, so each bucket has limit to prevent exponential growth -> each bucket contains less information about the space it needs to occupy.
 * You know more of the know you are close to.
*/
public class HistoryRoutingTable implements RoutingTable {

   private final Map<Bytes, UInt256> radiusMap;
   private final NodeTable nodeTable;
    //gossip ?


    public HistoryRoutingTable(final NodeRecord homeNode, final LivenessChecker livenessChecker) {
        this.radiusMap = new ConcurrentHashMap<Bytes, UInt256> ();
        this.nodeTable = new NodeTable(homeNode, livenessChecker);
    }



    @Override
    public void removeRadius(Bytes nodeId) {
        this.radiusMap.remove(nodeId);

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
    public void addNode(NodeRecord nodeRecord) {
         this.nodeTable.addNode(nodeRecord);
    }

}
