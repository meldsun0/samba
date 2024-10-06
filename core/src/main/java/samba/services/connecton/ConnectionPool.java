package samba.services.connecton;

import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.node.NodeId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {

    private final Map<NodeId, ConnectionState> nodesPool = new ConcurrentHashMap<>();


    public void updateLivenessNode(NodeId nodeId) {
        this.insertOrUpdate(nodeId, ConnectionState.CONNECTED);
    }

    private void insertOrUpdate(NodeId nodeId, ConnectionState state) {
        this.nodesPool.compute(nodeId, (key, value) -> state);
    }

    public void ignoreNode(UInt64 nodeId) {
        //this.insertOrUpdate(nodeId, ConnectionState.CONNECTED);
    }

    public int getNumberOfConnectedPeers() {

        return this.nodesPool.size();
    }

    public boolean isPeerConnected(NodeRecord peer) {
        return this.nodesPool.get(peer.getNodeId())!=null  && this.nodesPool.get(peer.getNodeId()).equals(ConnectionState.CONNECTED);
    }
}
