package samba.network.history;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentType;
import samba.domain.dht.LivenessChecker;
import samba.domain.messages.MessageType;
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
import samba.services.jsonrpc.methods.results.FindContentResult;
import samba.services.utp.UTPService;
import samba.storage.HistoryDB;

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
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class HistoryNetwork extends BaseNetwork
    implements HistoryJsonRpcRequests, HistoryNetworkIncomingRequests, LivenessChecker {

  private UInt256 nodeRadius;
  private final HistoryDB historyDB;
  final NodeRecordFactory nodeRecordFactory;
  protected RoutingTable routingTable;
  private final UTPService utpService;

  public HistoryNetwork(Discv5Client client, HistoryDB historyDB, UTPService utpService) {
    super(NetworkType.EXECUTION_HISTORY_NETWORK, client, UInt256.ONE);
    this.nodeRadius = UInt256.ONE; // TODO must come from argument
    this.routingTable = new HistoryRoutingTable(client.getHomeNodeRecord(), this);
    this.historyDB = historyDB;
    this.utpService = utpService;
    this.nodeRecordFactory = new NodeRecordFactory(new IdentitySchemaV4Interpreter());
  }

  @Override
  public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message) {
    return sendMessage(nodeRecord, message)
        .orTimeout(5, TimeUnit.SECONDS)
        .thenApply(Optional::get)
        .thenCompose(
            pongMessage -> {
              LOG.trace(
                  "{} message being processed from {}",
                  message.getMessageType(),
                  nodeRecord.asEnr());
              Pong pong = pongMessage.getMessage();
              if (pong.containsPayload()) {
                this.routingTable.addOrUpdateNode(nodeRecord);
                this.routingTable.updateRadius(
                    nodeRecord.getNodeId(), UInt256.fromBytes(pong.getCustomPayload()));
              } else {
                LOG.trace("{} message without payload", message.getMessageType());
              }
              return SafeFuture.completedFuture(Optional.of(pong));
            })
        .exceptionallyCompose(
            error -> {
              LOG.trace(
                  "Something when wrong when processing message {} to {}",
                  message.getMessageType(),
                  nodeRecord.asEnr());
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
              SafeFuture.runAsync(
                  () -> {
                    nodes.getEnrList().stream()
                        .map(nodeRecordFactory::fromEnr)
                        .filter(this::isNotHomeNode)
                        .filter(node -> !node.asEnr().equals(nodeRecord.asEnr()))
                        .filter(this::isPossibleNodeCandidate)
                        .forEach(
                            node ->
                                this.ping(
                                    node, new Ping(node.getSeq(), this.nodeRadius.toBytes())));
                  });
              return SafeFuture.completedFuture(Optional.of(nodes));
            })
        .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
  }


  // TODO validate what to answer if there is an error.
  @Override
  public SafeFuture<Optional<FindContentResult>> findContent(NodeRecord nodeRecord, FindContent message) {
    return sendMessage(nodeRecord, message)
            .thenApply(contentMessageOpt -> contentMessageOpt.orElse(null))
            .thenCompose(contentMessage -> {
                if (contentMessage == null) {
                  LOG.warn("Content message is empty for node: {}", nodeRecord.asEnr());
                return SafeFuture.completedFuture(Optional.empty());
              }
              Content content = contentMessage.getMessage();
                // TODO: Validate NodeRadius
                //ContentType contentType = ContentKey.decode(contentKey)
              return switch (content.getContentType()) {
                case Content.UTP_CONNECTION_ID -> requestContentThroughUTP(nodeRecord, content.getConnectionId());
                case Content.CONTENT_TYPE -> handleContentReceived(message.getContentKey(), content.getContent());
                case Content.ENRS -> handleEnrs(nodeRecord, content);
                default -> SafeFuture.completedFuture(Optional.of(new FindContentResult()));
              };
            });
  }

    // Handle UTP connection ID case
    private SafeFuture<Optional<FindContentResult>> requestContentThroughUTP(NodeRecord nodeRecord, int connectionID) {

        return this.utpService
                .getContent(nodeRecord, connectionID)
                .thenCompose(answer -> {
                    System.out.println(answer.toHexString());
                    //historyDB.saveContent(contentkey, content);
                    return SafeFuture.completedFuture(Optional.of(new FindContentResult(answer.toHexString(), true)));
                })
                .exceptionallyCompose(error -> {
                    LOG.trace("Error when getting content");
                    return SafeFuture.failedFuture(error);
                });
    }


    // ENR records of nodes that are closest to the requested content.
    private SafeFuture<Optional<FindContentResult>> handleEnrs(NodeRecord nodeRecord, Content content) {
        List<String> enrs = content.getEnrList();
        if (!enrs.isEmpty()) {
            // TODO: Process ENR list (filtering, decoding, etc.)
            //                    content.getEnrList().stream()
            //                        .map(RLPDecoder::decodeRlpEnr)
            //                        .filter(
            //                            enr ->
            //                                !enr.equals(nodeRecord.asEnr())
            //                                    ||
            // !enr.equals(this.discv5Client.getHomeNodeRecord().asEnr()))
            //                        .toList();
            // TODO what to do ?
        } else {
            LOG.info("Node: {} does not hold the requested content or knows any eligible node", nodeRecord.asEnr());
        }
        return SafeFuture.completedFuture(Optional.of(new FindContentResult(enrs)));
    }


    private SafeFuture<Optional<FindContentResult>> handleContentReceived(Bytes contentKey, Bytes content) {
      //   historyDB.saveContent(contentKey, content);
        // TODO: Save to historyDB or perform other actions
        //                  boolean successfullySaved =
//                      historyDB.saveContent(message.getContentKey(), content.getContent());
//                      if (successfullySaved) {
//                        // gossipNetwork
//                      }
        return SafeFuture.completedFuture(Optional.of(new FindContentResult(content.toHexString(),false)));
    }




  @Override
  public SafeFuture<Optional<Accept>> offer(NodeRecord nodeRecord, Offer message) {
    return sendMessage(nodeRecord, message)
        .thenApply(Optional::get)
        .thenCompose(
            acceptMessage -> {
              Accept accept = acceptMessage.getMessage();
              // TODO create UTP stream using connectionId
              return SafeFuture.completedFuture(Optional.of(accept));
            })
        .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
  }

  @Override
  public void addEnr(String enr) {
    final NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);
    this.routingTable.addOrUpdateNode(nodeRecord);
  }

  @Override
  public Optional<String> getEnr(String nodeId) {
    Bytes nodeIdInBytes = Bytes.fromHexString(nodeId);
    final NodeRecord homeNodeRecord = this.discv5Client.getHomeNodeRecord();
    if (homeNodeRecord == null) {
      return Optional.empty();
    }
    if (homeNodeRecord.getNodeId().equals(nodeIdInBytes)) {
      return Optional.of(homeNodeRecord.asEnr());
    }
    Optional<NodeRecord> nodeRecord = this.routingTable.findNode(nodeIdInBytes);

    return nodeRecord.map(NodeRecord::asEnr);
  }

  @Override
  public boolean deleteEnr(String nodeId) {
    Bytes nodeIdInBytes = Bytes.fromHexString(nodeId);
    Optional<NodeRecord> nodeRecordToBeRemoved = this.routingTable.findNode(nodeIdInBytes);
    nodeRecordToBeRemoved.ifPresent(
        this.routingTable
            ::removeNode); // TODO refactor inner functions to return the state of the operation.
    return nodeRecordToBeRemoved.isPresent();
  }

  @Override
  public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord) {
    Ping ping = new Ping(nodeRecord.getSeq(), this.nodeRadius.toBytes());
    return this.ping(nodeRecord, ping);
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
    findNodes
        .getDistances()
        .forEach(
            distance -> {
              if (distance == 0) {
                nodesPayload.add(this.getHomeNodeAsBase64());
              } else {
                // TODO Check max bytes to be sent and decide what to do if not the initialization
                // of Nodes will fail.
                this.routingTable
                    .getNodes(distance)
                    .filter(node -> !srcNode.asEnr().equals(node.asEnr()))
                    .forEach(node -> nodesPayload.add(node.asBase64()));
              }
            });
    return new Nodes(nodesPayload);
  }

  @Override
  public PortalWireMessage handleFindContent(NodeRecord srcNode, FindContent findContent) {
    Bytes contentKey = findContent.getContentKey();
    ContentType contentType = ContentType.fromContentKey(contentKey);
    Bytes value = contentKey.slice(0, 1);
    Optional<byte[]> content = historyDB.get(contentType, value);
    if (content.isEmpty()) {
      // TODO return list of ENRs that we know of that are closest to the requested content
      /*If the node does not hold the requested content, and the node does not know of any nodes with eligible ENR values, then the node MUST return enrs as an empty list.*/
      return new Content(List.of());
    }
    if (content.get().length > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
      // TODO initiate UTP connection and UTP async listen on connectionId
      return new Content(0);
    }
    return new Content(Bytes.of(content.get()));
  }

  @Override
  public PortalWireMessage handleOffer(NodeRecord srcNode, Offer offer) {
    BitSet missingContent = new BitSet(offer.getContentKeys().size());
    //    for (int i = 0; i < offer.getContentKeys().size(); i++) {
    //        if (!historyDB.contains(offer.getContentKeys().get(i))) {
    //            missingContent.set(i);
    //        }
    //    }

    int sliceLength = (offer.getContentKeys().size() + 7) / 8;
    Bytes contentKeysBitList = Bytes.wrap(missingContent.toByteArray()).slice(sliceLength);
    // int connectionId = UTP.generateConnectionId();
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
    Ping pingMessage = new Ping(nodeRecord.getSeq(), this.nodeRadius);
    return CompletableFuture.supplyAsync(() -> this.ping(nodeRecord, pingMessage))
        .thenCompose((__) -> new CompletableFuture<>());
  }

  private String getHomeNodeAsEnr() {
    return this.discv5Client.getHomeNodeRecord().asEnr();
  }

  private String getHomeNodeAsBase64() {
    return this.discv5Client.getHomeNodeRecord().asBase64();
  }

  private boolean isPossibleNodeCandidate(NodeRecord node) {
    return !this.routingTable.isNodeConnected(node.getNodeId())
        || !this.routingTable.isNodeIgnored(node);
  }

  private boolean isNotHomeNode(NodeRecord node) {
    return !(this.discv5Client.getHomeNodeRecord().equals(node));
  }

  @NotNull
  private static <V>
      Function<Throwable, CompletionStage<Optional<V>>> createDefaultErrorWhenSendingMessage(
          MessageType message) {
    return error -> {
      LOG.info("Something when wrong when sending a {}", message);
      return SafeFuture.completedFuture(Optional.empty());
    };
  }
}
