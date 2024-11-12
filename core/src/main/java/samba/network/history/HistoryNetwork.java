package samba.network.history;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.IdentitySchemaV4Interpreter;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

import org.jetbrains.annotations.NotNull;
import samba.domain.messages.MessageType;
import samba.storage.HistoryDB;
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

public class HistoryNetwork extends BaseNetwork implements HistoryNetworkRequests, HistoryNetworkIncomingRequests, LivenessChecker {

    private UInt256 nodeRadius;
    private final HistoryDB historyDB;
    final NodeRecordFactory nodeRecordFactory;
    protected RoutingTable routingTable;

    public HistoryNetwork(Discv5Client client, HistoryDB historyDB) {
        super(NetworkType.EXECUTION_HISTORY_NETWORK, client, UInt256.ONE);
        this.nodeRadius = UInt256.ONE; //TODO must come from argument
        this.routingTable = new HistoryRoutingTable(client.getHomeNodeRecord(), this);
        this.historyDB = historyDB;
        this.nodeRecordFactory = new NodeRecordFactory(new IdentitySchemaV4Interpreter());
        LOG.info("Home Record :" + client.getHomeNodeRecord().asEnr());
    }


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


    @Override
    public SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes message) {
        return sendMessage(nodeRecord, message)
                .orTimeout(3, TimeUnit.SECONDS)
                .thenApply(Optional::get)
                .thenCompose(
                        nodesMessage -> {
                            Nodes nodes = nodesMessage.getMessage();
                            //SafeFuture.runAsync(() -> {
                            nodes.getEnrList().stream()
                                    .map(nodeRecordFactory::fromEnr)
                                    .filter(this::isNotHomeNode)
                                    .filter(node -> !node.asEnr().equals(nodeRecord.asEnr()))
                                    .filter(this::isPossibleNodeCandidate)
                                    .forEach(node -> this.ping(node, new Ping(node.getSeq(), this.nodeRadius.toBytes())));
                            //   });
                            return SafeFuture.completedFuture(Optional.of(nodes));
                        })
                .exceptionallyCompose(
                        createDefaultErrorWhenSendingMessage(message.getMessageType()));
    }


    @Override
    public SafeFuture<Optional<Content>> findContent(NodeRecord nodeRecord, FindContent message) {
        return sendMessage(nodeRecord, message)
                .thenApply(Optional::get)
                .thenCompose(
                        contentMessage -> {
                            Content content = contentMessage.getMessage();

                            //If the node does not hold the requested content, and the node does not know of any nodes with eligible ENR values, then the node MUST return enrs as an empty list.

                            switch (content.getContentType()) {
                                case Content.UTP_CONNECTION_ID-> {/*
                                    SafeFuture.runAsync(() -> {
                                    //TODO async UTP opperation
                                    });*/
                                }
                                case Content.CONTENT_TYPE -> historyDB.saveContent(message.getContentKey(), content.getContent());
                                case Content.ENRS -> {
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
                .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
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
                .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
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
        List<String> nodesPayload = new ArrayList<>();
        findNodes.getDistances().forEach(distance -> {
            if (distance == 0) {
                nodesPayload.add(this.getHomeNodeAsEnr());
            } else {
                //TODO Check max bytes to be sent and decide what to do if not the initialization of Nodes will fail.
                this.routingTable.getNodes(distance)
                        .filter(node -> !srcNode.asEnr().equals(node.asEnr()))
                        .forEach(node -> nodesPayload.add(node.asEnr()));
            }
        });
        return new Nodes(nodesPayload);
    }

    @Override
    public PortalWireMessage handleFindContent(NodeRecord srcNode, FindContent findContent) {
        if (historyDB.contains(findContent.getContentKey())) {
            Bytes content = historyDB.get(findContent.getContentKey());
            if (content.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
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

    private String getHomeNodeAsEnr() {
        return this.discv5Client.getHomeNodeRecord().asEnr();
    }

    private boolean isPossibleNodeCandidate(NodeRecord node) {
        return !this.routingTable.isNodeConnected(node.getNodeId()) || !this.routingTable.isNodeIgnored(node);
    }

    private boolean isNotHomeNode(NodeRecord node) {
        return !(this.discv5Client.getHomeNodeRecord().equals(node));
    }

    @NotNull
    private static <V> Function<Throwable, CompletionStage<Optional<V>>> createDefaultErrorWhenSendingMessage(MessageType message) {
        return error -> {
            LOG.info("Something when wrong when sending a {}", message);
            return SafeFuture.completedFuture(Optional.empty());
        };
    }

}
