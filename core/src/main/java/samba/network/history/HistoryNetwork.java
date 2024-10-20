package samba.network.history;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Accept;
import samba.domain.messages.response.Content;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.services.connecton.ConnectionPool;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequestOperations {


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

    @Override
    public SafeFuture<Optional<Content>> findContent(NodeRecord nodeRecord, FindContent message) {
        return sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                        contentMessage -> {
                            Content content = contentMessage.getMessage();
                            //Parse for three subtypes and then opperate accordingly
                            if (content.getContentType() == 0) {
                                //uTP connection
                            } else if (content.getContentType() == 1) {
                                //Recieve content
                            } else if (content.getContentType() == 2) {
                                List<String> nodesList = content.getEnrList();
                                //nodesList.removeIf(nodeRecord::getSeq); //The ENR record of the requesting node SHOULD be filtered out of the list.
                                //nodesList.removeIf(node -> connectionPool.isIgnored(node.getSeq()));
                                //nodesList.removeIf(routingTable::isKnown);
                                //nodesList.forEach(this::pingUnknownNode);
                            }
                            return SafeFuture.completedFuture(Optional.of(content));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {}", message.getMessageType());
                            return SafeFuture.completedFuture(Optional.empty());
                        });
    }

    @Override
    public SafeFuture<Optional<Accept>> offer(NodeRecord nodeRecord, Offer message) {
        return sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                        acceptMessage -> {
                            Accept accept = acceptMessage.getMessage();
                            //TODO
                            return SafeFuture.completedFuture(Optional.of(accept));
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
