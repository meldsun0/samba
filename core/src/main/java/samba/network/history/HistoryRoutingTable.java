package samba.network.history;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import samba.domain.dht.LivenessChecker;
import samba.domain.dht.NodeTable;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.RoutingTable;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/** KBuckets: Represent distances from the local node perspective. It's a "binary tree whose leaves are k-buckets. The distance from myself is 0.
 * The binary tree exactly match the number of bits (256) in the key-space. (We have spot for any given distinction).
 * The greater the distance the greater the space, so each bucket has limit to prevent exponential growth -> each bucket contains less information about the space it needs to occupy.
 * You know more of the know you are close to.
 *
 * Radius: represents the data that a homeNode is "interested" in.
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

    public void updateRadius(Bytes nodeId, UInt256 radius) {
        this.radiusMap.put(nodeId, radius);
    }
    
    @Override
    public UInt256 getRadius(Bytes nodeId){
        return this.radiusMap.get(nodeId);
    }

    @Override
    public Optional<NodeRecord> findNode(Bytes nodeId) {
        return this.nodeTable.getNode(nodeId);
    }

    @Override
    public Stream<NodeRecord> getNodes(int distance) {
        return this.nodeTable.getLiveNodeRecords(distance);
    }

    @Override
    public void addOrUpdateNode(NodeRecord nodeRecord) {
         this.nodeTable.addNode(nodeRecord);
    }

    @Override
    public void removeNode(NodeRecord nodeRecord) {
        this.nodeTable.removeNode(nodeRecord);
    }

    @Override
    public int getActiveNodes() {
        return this.nodeTable.getStats().getTotalLiveNodeCount();
    }

    @Override
    public boolean isNodeConnected(Bytes nodeId) {
        return this.nodeTable.getNode(nodeId).isPresent();
    }

    @Override
    public boolean isNodeIgnored(NodeRecord nodeRecord) {
        return this.nodeTable.isNodeIgnored(nodeRecord);
    }
}
