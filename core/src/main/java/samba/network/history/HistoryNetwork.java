package samba.network.history;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.services.connecton.ConnectionPool;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;

public class HistoryNetwork extends BaseNetwork  implements HistoryNetworkRequestOperations {


    private NodeRecord nodeRecord;
    private ConnectionPool connectionPool;



    public HistoryNetwork(Discv5Client client){
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, new HistoryRoutingTable());
        this.connectionPool = new ConnectionPool();
    }



    /**
     * Sends a Portal Network Wire PING message to a specified node
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message PING message to be sent
     * @return the PONG message.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message ) { //node should be changed.
        //avoid pinging ourself.
        //handle timeout
        return  sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                       pongMessage -> {
                              LOG.trace("{} message received from {}", message.getType(), message.getEnrSeq().get());
                              Pong pong = pongMessage.getMessage();
                              connectionPool.updateLivenessNode(pong.getNodeId());
                              if(pong.getCustomPayload() != null){ //TO-DO decide what to validate.
                                 this.routingTable.updateRadius(pong.getNodeId(), 1);
                                 //should we need to notify someone ?
                              }
                            return SafeFuture.completedFuture(Optional.of(pong));
                       })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {} to {}", message.getType() , message.getEnrSeq().get());
                            this.connectionPool.ignoreNode(message.getEnrSeq().get());
                            this.routingTable.evictNode(message.getEnrSeq().get());
                            return SafeFuture.completedFuture(Optional.empty());
                        });

    }

    @Override
    public SafeFuture<NodeRecord> connect(NodeRecord peer) {
       return  this.ping(peer, new Ping(peer.getSeq(),  new byte[]{})).thenApply(Optional::get).thenCompose(pong -> {
              return SafeFuture.completedFuture(pong.getNodeRecord());
       });
    }

    @Override
    public int getPeerCount() {
        return connectionPool.getNumberOfConnectedPeers();
    }

    @Override
    public boolean isPeerConnected(NodeRecord peer) {
        return this.connectionPool.isPeerConnected(peer);
    }
}
