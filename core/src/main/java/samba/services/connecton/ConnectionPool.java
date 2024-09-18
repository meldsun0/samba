package samba.services.connecton;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.node.NodeId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {

    private final Map<NodeId, ConnectionState> nodesPool = new ConcurrentHashMap<>();


    private void insertOrUpdate(NodeId nodeId, ConnectionState state){
        this.nodesPool.compute(nodeId, (key, value) -> state);
    }


    public void updateLivenessNode(NodeId nodeId){
        this.insertOrUpdate(nodeId, ConnectionState.CONNECTED);
    }

    public void ignoreNode(UInt64 uInt64) {

    }
}
