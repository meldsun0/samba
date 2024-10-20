package samba.services.connecton;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool {

    //Node-ID
    private final Map<Bytes, ConnectionState> nodesPool = new ConcurrentHashMap<>(); //TODO Define size


    public void updateLivenessNode(Bytes nodeId) {
        this.insertOrUpdate(nodeId, ConnectionState.CONNECTED);
    }

    private void insertOrUpdate(Bytes nodeId, ConnectionState state) {
        this.nodesPool.compute(nodeId, (key, value) -> state);
    }

    public void ignoreNode(Bytes nodeId) {
       //TODO what if, if it is not present ?
        this.insertOrUpdate(nodeId, ConnectionState.IGNORED);
    }

    public int getNumberOfConnectedPeers() { //TODO use an atomic integer
        return Collections.frequency(new ArrayList<ConnectionState>(this.nodesPool.values()), ConnectionState.CONNECTED);
    }

    public boolean isPeerConnected(Bytes nodeId) {
        return checkStatus(nodeId, ConnectionState.CONNECTED);
    }

    public boolean isIgnored(Bytes nodeId) {
        return checkStatus(nodeId, ConnectionState.IGNORED);
    }

    private boolean checkStatus(Bytes nodeId, ConnectionState state){
        return this.nodesPool.containsKey(nodeId) && this.nodesPool.get(nodeId).equals(state);
    }
}

