package samba.network.history;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.NodesV2;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.services.connecton.ConnectionPool;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;



public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequestOperations {



    private ConnectionPool connectionPool;


    public HistoryNetwork(Discv5Client client) {
        //TODO
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client,
                new HistoryRoutingTable(), null);
        this.connectionPool = new ConnectionPool();
    }


    /**
     * Sends a Portal Network Wire PING message to a specified node
     *
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message    PING message to be sent
     * @return optional with  PONG message or empty if error.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) {
        //TODO validate before sending request
        return sendMessage(nodeRecord, message)
                .orTimeout(2, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        pongMessage -> {
                            Pong pong = pongMessage.getMessage();
                            connectionPool.updateLivenessNode(pong.getEnrSeq());
                            if (pong.getCustomPayload() != null) { //TO-DO decide what to validate.
                                this.routingTable.updateRadius(pong.getEnrSeq(), null); //get Radius
                                //should we need to notify someone ?
                            }
                            return SafeFuture.completedFuture(Optional.of(pong));
                        })
                .exceptionallyCompose(
                        error -> {
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
    public SafeFuture<Optional<NodesV2>> findNodes(NodeRecord nodeRecord, FindNodes message) {
         return sendMessage(nodeRecord, message)
                .orTimeout(3, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        nodesMessage -> {
                            NodesV2 nodes = nodesMessage.getMessage();
                            if (!nodes.isNodeListEmpty()) {
                                SafeFuture.runAsync(() -> {
                                    List<NodeRecord> nodesList = nodes.getNodes();
                                    nodesList.removeIf(nodeRecord::equals); //The ENR record of the requesting node SHOULD be filtered out of the list.
                                    nodesList.removeIf(node -> connectionPool.isIgnored(node.getSeq()));
                                    nodesList.removeIf(routingTable::isKnown);
                                    nodesList.forEach(this::pingUnknownNode);
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
        this.ping(nodeRecord, new Ping(nodeRecord.getSeq(), Bytes.EMPTY));  //TODO it should be this.nodeRadius
    }


    @Override
    public SafeFuture<NodeRecord> connect(NodeRecord peer) {
        return this.ping(peer, new Ping(peer.getSeq(), Bytes.EMPTY))
                .thenApply(Optional::get)
                .thenCompose(pong -> SafeFuture.completedFuture(pong.getNodeRecord()));
    }

    @Override
    public int getPeerCount() {
        return connectionPool.getNumberOfConnectedPeers();
    }

    @Override
    public boolean isPeerConnected(NodeRecord peer) {
        return this.connectionPool.isPeerConnected(peer.getSeq());
    }
}
