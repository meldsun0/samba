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

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequestOperations {


    private NodeRecord nodeRecord;
    private ConnectionPool connectionPool;


    public HistoryNetwork(Discv5Client client) {
        //TODO
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, new HistoryRoutingTable(null, null, null));
        this.connectionPool = new ConnectionPool();
    }


    /**
     * Sends a Portal Network Wire PING message to a specified node
     *
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message    PING message to be sent
     * @return a PONG message.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) { //node should be changed.
        if(nodeRecord.equals(this.nodeRecord)){
            LOG.info("Can not ping ourself");
            return SafeFuture.completedFuture(Optional.empty());
        }
        return sendMessage(nodeRecord, message)
                .orTimeout(2, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        pongMessage -> {
                            Pong pong = pongMessage.getMessage();
                            connectionPool.updateLivenessNode(pong.getNodeId());
                            if (pong.getCustomPayload() != null) { //TO-DO decide what to validate.
                                this.routingTable.updateRadius(pong.getNodeId(), null); //getRadius
                                //should we need to notify someone ?
                            }
                            return SafeFuture.completedFuture(Optional.of(pong));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {} to {}", message.getMessageType(), message.getEnrSeq().get());
                            this.connectionPool.ignoreNode(message.getEnrSeq().get());
                            this.routingTable.evictNode(message.getEnrSeq().get());
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
        //Each distance MUST be within the inclusive range [0, 256]
        //Each distance in the list MUST be unique.
        //  It is invalid to return multiple ENR records for the same node_id.
        return sendMessage(nodeRecord, message)
                .orTimeout(3, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        nodesMessage -> {
                            NodesV2 nodes = nodesMessage.getMessage();
                            if (!nodes.isNodeListEmpty()) {
                                SafeFuture.runAsync(() -> {
                                    nodes.getNodes().stream()
                                            .filter(n -> !nodeRecord.equals(n)) //The ENR record of the requesting node SHOULD be filtered out of the list.
                                            .filter(this::isNotIgnored)
                                            .filter(this::isNotKnown)
                                            .forEach(this::pingUnknownNode);
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
        this.ping(nodeRecord, new Ping(nodeRecord.getSeq(), Bytes.EMPTY));  //TODO Payload should be empty?
    }

    private boolean isNotKnown(NodeRecord nodeRecord) {
        return !this.routingTable.isKnown(nodeRecord);
    }

    private boolean isNotIgnored(NodeRecord nodeRecord) {
        return !this.connectionPool.isIgnored(nodeRecord);
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
        return this.connectionPool.isPeerConnected(peer);
    }
}
