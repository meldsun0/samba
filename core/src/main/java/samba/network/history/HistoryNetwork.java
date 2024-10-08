package samba.network.history;


import org.apache.tuweni.bytes.Bytes;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.services.connecton.ConnectionPool;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HistoryNetwork extends BaseNetwork  implements HistoryNetworkRequestOperations {


    private NodeRecord nodeRecord;
    private ConnectionPool connectionPool;



    public HistoryNetwork(Discv5Client client){
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, new HistoryRoutingTable(), null);
        this.connectionPool = new ConnectionPool();
    }



    /**
     * Sends a Portal Network Wire PING message to a specified node
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message PING message to be sent
     * @return the PONG message.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) { //node should be changed.
        //avoid pinging ourself.
        //handle timeout
        return  sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                       pongMessage -> {
                              LOG.trace("{} message received from {}", message.getMessageType(), message.getEnrSeq());
                              Pong pong = pongMessage.getMessage();
                              connectionPool.updateLivenessNode(pong.getEnrSeq());
                              if(pong.getCustomPayload() != null){ //TO-DO decide what to validate.
                                 this.routingTable.updateRadius(pong.getEnrSeq(), null); // TODO getRadius
                                 //should we need to notify someone ?
                              }
                            return SafeFuture.completedFuture(Optional.of(pong));
                       })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {} to {}", message.getMessageType(), message.getEnrSeq());
                            this.connectionPool.ignoreNode(message.getEnrSeq());
                            this.routingTable.evictNode(message.getEnrSeq());
                            return SafeFuture.completedFuture(Optional.empty());
                        });

    }

    /**
     * Sends a Portal Network Wire FINDNODES message request to a peer requesting other node ENRs
     *
     * @param nodeRecord the nodeId of the peer to send the findnodes message
     * @param message    FINDNODES message to be sent
     * @return a FINDNODES message.
     */
    @Override
    public SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes message) {
         return sendMessage(nodeRecord, message)
                .orTimeout(3, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        nodesMessage -> {
                            Nodes nodes = nodesMessage.getMessage();
                            if (!nodes.isNodeListEmpty()) {
                                SafeFuture.runAsync(() -> {
                                    List<String> nodesList = nodes.getEnrList();
//                                    nodesList.removeIf(nodeRecord::getSeq); //The ENR record of the requesting node SHOULD be filtered out of the list.
//                                    nodesList.removeIf(node -> connectionPool.isIgnored(node.getSeq()));
//                                    nodesList.removeIf(routingTable::isKnown);
//                                    nodesList.forEach(this::pingUnknownNode);
                                });
                            }
                            return SafeFuture.completedFuture(Optional.of(nodes));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {}", message.getMessageType());
                            return SafeFuture.completedFuture(Optional.empty());
                        });
    }

    private void pingUnknownNode(NodeRecord nodeRecord) {
        this.ping(nodeRecord, new Ping(UInt64.valueOf(nodeRecord.getSeq().toBytes().toLong()), Bytes.EMPTY));  //TODO it should be this.nodeRadius
    }


    @Override
    public SafeFuture<NodeRecord> connect(NodeRecord peer) {
       return  this.ping(peer, new Ping(UInt64.valueOf(peer.getSeq().toBytes().toLong()), Bytes.EMPTY)).thenApply(Optional::get).thenCompose(pong -> {
              return SafeFuture.completedFuture(pong.getNodeRecord());
       });
    }

    @Override
    public int getPeerCount() {
        return connectionPool.getNumberOfConnectedPeers();
    }

    @Override
    public boolean isPeerConnected(NodeRecord peer) {
        return this.connectionPool.isPeerConnected(UInt64.valueOf(peer.getSeq().toBytes().toLong()));
    }
}
