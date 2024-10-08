package samba.services.connecton;

import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.node.NodeId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {

    private final Map<UInt64, ConnectionState> nodesPool = new ConcurrentHashMap<>();


    public void updateLivenessNode(UInt64 nodeId) {
        this.insertOrUpdate(nodeId, ConnectionState.CONNECTED);
    }

    private void insertOrUpdate(UInt64 nodeId, ConnectionState state) {
        this.nodesPool.compute(nodeId, (key, value) -> state);
    }

    public void ignoreNode(UInt64 nodeId) {
       //TODO what if, if it is not present ?
        this.nodesPool.computeIfPresent(
               nodeId, (key, currentValue )-> ConnectionState.IGNORED);
    }

    public int getNumberOfConnectedPeers() {

        return this.nodesPool.size();
    }

    public boolean isPeerConnected(UInt64 nodeId) {
        return checkStatus(nodeId, ConnectionState.CONNECTED);
    }


    public boolean isIgnored(UInt64 nodeId) {
        return checkStatus(nodeId, ConnectionState.IGNORED);
    }

    private boolean checkStatus(UInt64 nodeId, ConnectionState state){
        return this.nodesPool.containsKey(nodeId) && this.nodesPool.get(nodeId).equals(state);
    }
}
