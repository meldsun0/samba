package samba.network.history;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.db.PortalDB;
import samba.domain.dht.LivenessChecker;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Accept;
import samba.domain.messages.response.Content;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.network.BaseNetwork;
import samba.network.NetworkType;
import samba.network.RoutingTable;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

//TODO replace NodeRecord.
public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequests, HistoryNetworkIncomingRequests, LivenessChecker {


    private UInt256 nodeRadius;
    private PortalDB historyDB;

    protected RoutingTable routingTable;

    public HistoryNetwork(Discv5Client client) {
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, UInt256.ONE);
        this.nodeRadius = UInt256.ONE; //TODO must come from argument
        this.routingTable = new HistoryRoutingTable(client.getHomeNodeRecord(), this);
    }


    /**
     * Sends a Portal Network Wire PING message to a specified node
     *
     * @param nodeRecord the nodeId of the peer to send a ping to
     * @param message    PING message to be sent
     * @return the PONG message.
     */
    @Override
    public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) {
        return sendMessage(nodeRecord, message)
                .orTimeout(30, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        pongMessage -> {
                            LOG.trace("{} message being processed from {}", message.getMessageType(), nodeRecord.asEnr());
                            Pong pong = pongMessage.getMessage();
                            if (pong.containsPayload()) {
                                this.routingTable.addOrUpdateNode(nodeRecord);
                                this.routingTable.updateRadius(nodeRecord.getNodeId(), UInt256.fromBytes(pong.getCustomPayload()));
                            } else {
                                LOG.trace("{} message without payload", message.getMessageType());
                            }
                            return SafeFuture.completedFuture(Optional.of(pong));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.trace("Something when wrong when processing message {} to {}", message.getMessageType(), nodeRecord.asEnr());
                            this.routingTable.removeNode(nodeRecord);
                            this.routingTable.removeRadius(nodeRecord.getNodeId());
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
                            switch (content.getContentType()) {
                                case 0 -> {/*
                                    SafeFuture.runAsync(() -> {
                                    //TODO async UTP opperation
                                    });*/}
                                case 1 -> historyDB.put(message.getContentKey(), content.getContent());
                                case 2 -> {
                                    List<String> nodesList = content.getEnrList();
                                    //nodesList.removeIf(nodeRecord::getSeq); //The ENR record of the requesting node SHOULD be filtered out of the list.
                                    //nodesList.removeIf(node -> connectionPool.isIgnored(node.getSeq()));
                                    //nodesList.removeIf(routingTable::isKnown);
                                    //nodesList.forEach(this::pingUnknownNode);
                                    }
                                default -> throw new IllegalArgumentException("CONTENT: Invalid payload type");
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
                            //TODO create UTP stream using connectionId
                            return SafeFuture.completedFuture(Optional.of(accept));
                        })
                .exceptionallyCompose(
                        error -> {
                            LOG.info("Something when wrong when sending a {}", message.getMessageType());
                            return SafeFuture.completedFuture(Optional.empty());
                        });
    }

    @Override
    public SafeFuture<String> connect(NodeRecord nodeRecord) {
        Ping ping = new Ping(nodeRecord.getSeq(), this.nodeRadius.toBytes());
        return this.ping(nodeRecord, ping).thenApply(Optional::get).thenCompose(pong -> {
            return SafeFuture.completedFuture(pong.getEnrSeq().toString());
        });
    }

    @Override
    public int getNumberOfConnectedPeers() {
        return routingTable.getActiveNodes();
    }

    @Override
    public boolean isNodeConnected(NodeRecord nodeId) {
        return this.routingTable.isNodeConnected(nodeId.getNodeId());
    }


    @Override
    public UInt256 getRadiusFromNode(NodeRecord node) {
        return this.routingTable.getRadius(node.getNodeId());
    }

    @Override
    public PortalWireMessage handlePing(NodeRecord srcNode, Ping ping) {
        Bytes srcNodeId = srcNode.getNodeId();
        routingTable.addOrUpdateNode(srcNode);
        routingTable.updateRadius(srcNodeId, UInt256.fromBytes(ping.getCustomPayload()));
        return new Pong(getLocalEnrSeg(), this.nodeRadius.toBytes());
    }

    @Override
    public PortalWireMessage handleFindNodes(NodeRecord srcNode, FindNodes findNodes) {
        return null;
    }

    @Override
    public PortalWireMessage handleFindContent(NodeRecord srcNode, FindContent findContent) {
        if (historyDB.contains(findContent.getContentKey())) {
            Bytes content = historyDB.get(findContent.getContentKey());
            if (content.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                //TODO initiate UTP connection
                //int connectionId = UTP.generateConnectionId();
                //UTP async listen on connectionId
                return new Content(0);
            } else {
                return new Content(content);
            }
        } else {
            //TODO return list of ENRs that we know of that are closest to the requested content
            return new Content(List.of());
        }
    }

    @Override
    public PortalWireMessage handleOffer(NodeRecord srcNode, Offer offer) {
        BitSet missingContent = new BitSet(offer.getContentKeys().size());
        for (int i = 0; i < offer.getContentKeys().size(); i++) {
            if (!historyDB.contains(offer.getContentKeys().get(i))) {
                missingContent.set(i);
            }
        }

        int sliceLength = (offer.getContentKeys().size() + 7) / 8;
        Bytes contentKeysBitList = Bytes.wrap(missingContent.toByteArray()).slice(sliceLength);
        //int connectionId = UTP.generateConnectionId();
        Accept accept = new Accept(0, contentKeysBitList);
        return accept;
    }

    private org.apache.tuweni.units.bigints.UInt64 getLocalEnrSeg() {
        return discv5Client.getEnrSeq();
    }

    @Override
    public NetworkType getNetworkType() {
        return networkType;
    }

    @Override
    public CompletableFuture<Void> checkLiveness(NodeRecord nodeRecord) {
        LOG.info("checkLiveness");
        Ping pingMessage = new Ping(UInt64.valueOf(nodeRecord.getSeq().toBytes().toLong()), this.nodeRadius);
        return CompletableFuture.supplyAsync(() -> this.ping(nodeRecord, pingMessage)).thenCompose((__) -> new CompletableFuture<>());
    }
}
