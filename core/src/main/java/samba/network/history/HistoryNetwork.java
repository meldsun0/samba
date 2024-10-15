package samba.network.history;


import org.apache.tuweni.units.bigints.UInt256;
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

public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequests, HistoryNetworkIncomingRequests {


    private ConnectionPool connectionPool;
    private UInt256 nodeRadius;


    public HistoryNetwork(Discv5Client client) {
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, new HistoryRoutingTable(), null);
        this.connectionPool = new ConnectionPool();
        this.nodeRadius = UInt256.ONE; //TODO must come from argument
    }


    /**
     * Sends a Portal Network Wire PING message to a specified node
     *
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message    PING message to be sent
     * @return the PONG message.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) { //TODO replace NodeRecord.
        return sendMessage(nodeRecord, message)
                .orTimeout(300, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        pongMessage -> {
                            LOG.trace("{} message being processed from {}", message.getMessageType(), message.getEnrSeq());
                            Pong pong = pongMessage.getMessage();
                            connectionPool.updateLivenessNode(pong.getEnrSeq());
                            if (pong.getCustomPayload() != null) {
                                //TODO decide what to validate.
                                this.routingTable.updateRadius(pong.getEnrSeq(), UInt256.fromBytes(pong.getCustomPayload()));
                                //TODO should we need to notify someone ?
                            }
                            return SafeFuture.completedFuture(Optional.of(pong));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when processing message {} to {}", message.getMessageType(), message.getEnrSeq());
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
        this.ping(nodeRecord, new Ping(UInt64.valueOf(nodeRecord.getSeq().toBytes().toLong()), this.nodeRadius.toBytes()));
    }


    @Override
    public SafeFuture<String> connect(NodeRecord peer) {
        Ping ping = new Ping(UInt64.valueOf(peer.getSeq().toBytes().toLong()), this.nodeRadius.toBytes());
        return this.ping(peer, ping).thenApply(Optional::get).thenCompose(pong -> {
                    return SafeFuture.completedFuture(pong.getEnrSeq().toString());
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

    @Override
    public void handlePing(NodeRecord srcNode, Ping ping) {

        connectionPool.updateLivenessNode(ping.getEnrSeq());
        routingTable.updateRadius(ping.getEnrSeq(), UInt256.fromBytes(ping.getCustomPayload()));

        Pong pong = new Pong(UInt64.valueOf(srcNode.getSeq().toBytes().toLong()), this.nodeRadius.toBytes());
        sendMessage(srcNode, pong);
    }

    @Override
    public NetworkType getNetworkType() {
        return networkType;
    }
}
