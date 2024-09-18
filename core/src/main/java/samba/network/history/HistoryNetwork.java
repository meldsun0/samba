package samba.network.history;


import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.*;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.network.PeerClient;
import samba.services.connecton.ConnectionPool;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HistoryNetwork extends BaseNetwork  implements HistoryNetworkRequestOperations {


    private NodeRecord nodeRecord;
    private ConnectionPool connectionPool;



    public HistoryNetwork(PeerClient client){
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client);
    }



    /**
     * Sends a Portal Network Wire PING message to a specified node
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message PING message to be sent
     * @returns the PING payload specified by the subnetwork or undefined
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message ) { //node should be changed.
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
                                 this.routingTable.updateRadius(pong.getNodeId(), pong.getRadius());
                                 //should we need to notify someone ?
                              }
                            return SafeFuture.completedFuture(Optional.of(pong));
                       })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a Ping to {}",message.getEnrSeq());
                            this.connectionPool.ignoreNode(message.getEnrSeq().get());
                            this.routingTable.evitNode(message.getEnrSeq().get());
                            return SafeFuture.completedFuture(Optional.empty());
                        });

    }

    //livenesscheck



}
