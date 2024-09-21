package samba.network.history;


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
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client);
        this.connectionPool = new ConnectionPool();


    }



    /**
     * Sends a Portal Network Wire PING message to a specified node
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message PING message to be sent
     * @returns the PING payload specified by the subnetwork or undefined
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message ) { //node should be changed.
        LOG.info("Send Ping message to {}", nodeRecord);
        //avoid pinging ourself.
        //handle timeout

        return  sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                       pongMessage -> {
                              LOG.info("Pong message received from {}", message.getEnrSeq());
                              Pong pong = pongMessage.getDeserilizedMessage();
                              connectionPool.updateLivenessNode(pong.getNodeId());
                              if(pong.getCustomPayload() != null){ //TO-DO decide what to validate.
                                 this.routingTable.updateRadius(pong.getNodeId(), 1);
                                 //should we need to notify someone ?
                              }
                            return SafeFuture.completedFuture(Optional.of(pong));
                       })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a Ping to {}",message.getEnrSeq());
                            this.connectionPool.ignoreNode(message.getEnrSeq().get());
                            this.routingTable.evictNode(message.getEnrSeq().get());
                            return SafeFuture.completedFuture(Optional.empty());
                        });

    }

    @Override
    public SafeFuture<NodeRecord> connect(NodeRecord peer) {
       return  this.ping(peer, null).thenApply(Optional::get).thenCompose(pong -> {
              return SafeFuture.completedFuture(new NodeRecordBuilder().build());
       });
    }

    @Override
    public int getPeerCount() {
        return connectionPool.getNumberOfConnectedPeers();
    }

    //livenesscheck



}
