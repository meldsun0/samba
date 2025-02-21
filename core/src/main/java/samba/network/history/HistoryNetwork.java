package samba.network.history;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.domain.dht.LivenessChecker;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.extensions.standard.ClientInfoAndCapabilities;
import samba.domain.messages.extensions.standard.ErrorExtension;
import samba.domain.messages.extensions.standard.ErrorType;
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
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;
import samba.util.Util;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

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
  private final UTPManager utpManager;

  // TODO standard client version text, standard capabilities list, standard client info extension,
  // default ping with standard arguments

  public HistoryNetwork(
      final Discv5Client client, final HistoryDB historyDB, final UTPManager utpManager) {
    super(NetworkType.EXECUTION_HISTORY_NETWORK, client, UInt256.ONE);
    this.nodeRadius = UInt256.ONE; // TODO must come from argument
    this.routingTable = new HistoryRoutingTable(client.getHomeNodeRecord(), this);
    this.historyDB = historyDB;
    this.utpManager = utpManager;
    this.nodeRecordFactory = new NodeRecordFactory(new IdentitySchemaV4Interpreter());
  }

  // TODO check on everymethod if null is received.

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
                ExtensionType pongExtensionType =
                    ExtensionType.fromValue(pong.getPayloadType().getValue());
                switch (pongExtensionType) {
                  case CLIENT_INFO_AND_CAPABILITIES -> {
                    try {
                      UInt256 dataRadius =
                          ClientInfoAndCapabilities.getDataRadiusFromSszBytes(pong.getPayload());
                      this.routingTable.addOrUpdateNode(nodeRecord);
                      this.routingTable.updateRadius(nodeRecord.getNodeId(), dataRadius);
                    } catch (Exception e) {
                      LOG.trace(
                          "Error {} when processing ClientInfoAndCapabilities {} for {}",
                          e,
                          pong.getPayload(),
                          pong.getMessageType());
                    }
                  }
                  case HISTORY_RADIUS -> { // TODO when history radius extension is implemented
                  }
                  case ERROR -> {
                    try {
                      ErrorExtension error = ErrorExtension.fromSszBytes(pong.getPayload());
                      LOG.trace(
                          "Error {} from recipient when processing Ping extension {} for {}",
                          ErrorType.fromCode(error.getErrorCode().getValue()),
                          ExtensionType.fromValue(message.getPayloadType().getValue()),
                          message.getPayload());
                    } catch (Exception e) {
                      LOG.trace(
                          "Error {} when processing ErrorExtension {} for {}",
                          e,
                          pong.getPayload(),
                          pong.getMessageType());
                    }
                  }
                  default -> {}
                }
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
                                    node,
                                    new Ping(
                                        node.getSeq(),
                                        ExtensionType.CLIENT_INFO_AND_CAPABILITIES
                                            .getExtensionCode(),
                                        new ClientInfoAndCapabilities(this.nodeRadius)
                                            .getSszBytes())));
                  });
              return SafeFuture.completedFuture(Optional.of(nodes));
            })
        .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
  }

  @Override
  public SafeFuture<Optional<FindContentResult>> findContent(
      NodeRecord nodeRecord, FindContent message) {
    return sendMessage(nodeRecord, message)
        .orTimeout(3, TimeUnit.SECONDS)
        .thenApply(Optional::get)
        .thenCompose(
            contentMessage -> {
              Content content = contentMessage.getMessage();
              // TODO: Validate NodeRadius
              return switch (content.getContentType()) {
                case Content.UTP_CONNECTION_ID ->
                    this.utpManager
                        .findContentRead(nodeRecord, content.getConnectionId())
                        .thenCompose(
                            data -> {
                              historyDB.saveContent(message.getContentKey(), data);
                              return SafeFuture.completedFuture(
                                  Optional.of(new FindContentResult(data.toHexString(), true)));
                            })
                        .exceptionallyCompose(
                            createDefaultErrorWhenSendingMessage(message.getMessageType()));
                case Content.CONTENT_TYPE -> {
                  // TODO validate content and key before persisting it or responding
                  // TODO Gossip new content to network -> trigger a lookup: Query X nearest  until

                  historyDB.saveContent(message.getContentKey(), content.getContent());
                  yield SafeFuture.completedFuture(
                      Optional.of(
                          new FindContentResult(content.getContent().toHexString(), false)));
                }
                case Content.ENRS -> {
                  List<String> enrs =
                      content.getEnrList().stream()
                          .filter(enr -> !enr.equals(this.discv5Client.getEnr().get()))
                          .toList();

                  yield SafeFuture.completedFuture(Optional.of(new FindContentResult(enrs)));
                }
                default -> SafeFuture.completedFuture(Optional.empty());
              };
            })
        .exceptionallyCompose(createDefaultErrorWhenSendingMessage(message.getMessageType()));
  }

  @Override
  public SafeFuture<Optional<Bytes>> offer(
      NodeRecord nodeRecord, List<Bytes> content, Offer message) {
    checkArgument(nodeRecord != null, "NodeRecord must not be null");
    checkArgument(content != null && !content.isEmpty(), "Content must not be empty");
    checkArgument(
        message != null && !message.getContentKeys().isEmpty(), "Offer must not be empty");
    checkArgument(
        content.size() == message.getContentKeys().size(),
        "There should be same contentItems and contentKeys");

    return sendMessage(nodeRecord, message)
        .thenApply(Optional::get)
        .thenCompose(
            acceptMessage -> {
              Accept accept = acceptMessage.getMessage();
              byte[] acceptedContent = accept.getContentKeysByteArray();
              List<Bytes> contentToOffer =
                  IntStream.range(0, acceptedContent.length)
                      .mapToObj(
                          idx -> {
                            if (acceptedContent[idx] == 0) return null;
                            Bytes currentContent = content.get(idx);
                            // TODO validate if is needed to go to the db.
                            if (currentContent.isEmpty()) {
                              Bytes contentKey = message.getContentKeys().get(idx);
                              return historyDB
                                  .get(ContentKey.decode(contentKey))
                                  .orElse(currentContent);
                            }
                            return currentContent;
                          })
                      .filter(Objects::nonNull)
                      .map(data -> Bytes.concatenate(Util.writeUnsignedLeb128(data.size()), data))
                      .toList();

              Optional.ofNullable(contentToOffer)
                  .filter(list -> !list.isEmpty())
                  .ifPresent(
                      list ->
                          utpManager.offerWrite(
                              nodeRecord, accept.getConnectionId(), Bytes.concatenate(list)));

              return SafeFuture.completedFuture(Optional.of(accept.getContentKeys()));
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
  public boolean store(Bytes contentKey, Bytes contentValue) {
    return this.historyDB.saveContent(contentKey, contentValue);
  }

  @Override
  public Optional<String> getLocalContent(ContentKey contentKey) {
    return this.historyDB.get(contentKey).map(Bytes::toHexString);
  }

  @Override
  public SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord) {
    Ping ping =
        new Ping(
            nodeRecord.getSeq(),
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            new ClientInfoAndCapabilities(this.nodeRadius).getSszBytes());
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

  public Optional<NodeRecord> findClosestNodeToContentKey(Bytes contentKey) {
    return this.routingTable.findClosestNodeToContentKey(contentKey);
  }

  public Optional<NodeRecord> nodeRecordFromEnr(String enr) {
    try {
      return Optional.ofNullable(nodeRecordFactory.fromEnr(enr));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public PortalWireMessage handlePing(NodeRecord srcNode, Ping ping) {
    try {
      ExtensionType pingExtensionType = ExtensionType.fromValue(ping.getPayloadType().getValue());
      switch (pingExtensionType) {
        case CLIENT_INFO_AND_CAPABILITIES -> {
          try {
            UInt256 dataRadius =
                ClientInfoAndCapabilities.getDataRadiusFromSszBytes(ping.getPayload());
            this.routingTable.addOrUpdateNode(srcNode);
            this.routingTable.updateRadius(srcNode.getNodeId(), dataRadius);
            return new Pong(
                getLocalEnrSeq(),
                ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
                new ClientInfoAndCapabilities(this.nodeRadius).getSszBytes());
          } catch (Exception e) {
            LOG.trace(
                "Error {} when processing ClientInfoAndCapabilities {} for {}",
                e,
                ping.getPayload(),
                ping.getMessageType());
            return new Pong(
                getLocalEnrSeq(),
                ExtensionType.ERROR.getExtensionCode(),
                new ErrorExtension(ErrorType.FAILED_TO_DECODE.getErrorCode()).getSszBytes());
          }
        }
        case HISTORY_RADIUS -> {
          // TODO when history radius extension is implemented
          return new Pong(
              getLocalEnrSeq(),
              ExtensionType.ERROR.getExtensionCode(),
              new ErrorExtension(ErrorType.EXTENSION_NOT_SUPPORTED.getErrorCode()).getSszBytes());
        }
        case ERROR -> {
          // TODO unexpected behavior, respond with error?
          return new Pong(
              getLocalEnrSeq(),
              ExtensionType.ERROR.getExtensionCode(),
              new ErrorExtension(ErrorType.SYSTEM_ERROR.getErrorCode()).getSszBytes());
        }
        default -> {
          return new Pong(
              getLocalEnrSeq(),
              ExtensionType.ERROR.getExtensionCode(),
              new ErrorExtension(ErrorType.EXTENSION_NOT_SUPPORTED.getErrorCode()).getSszBytes());
        }
      }
    } catch (Exception e) {
      return new Pong(
          getLocalEnrSeq(),
          ExtensionType.ERROR.getExtensionCode(),
          new ErrorExtension(ErrorType.EXTENSION_NOT_SUPPORTED.getErrorCode()).getSszBytes());
    }
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
  public PortalWireMessage handleFindContent(NodeRecord nodeRecord, FindContent findContent) {
    ContentKey contentKey =
        ContentUtil.createContentKeyFromSszBytes(findContent.getContentKey()).get();
    Optional<Bytes> content = historyDB.get(contentKey);
    if (content.isEmpty()) {
      // TODO return list of ENRs that we know of that are closest to the requested content
      return new Content(List.of());
    }
    if (content.get().size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
      int connectionId = this.utpManager.foundContentWrite(nodeRecord, content.get());

      return new Content(connectionId);
    }
    return new Content(content.get());
  }

  @Override
  public PortalWireMessage handleOffer(NodeRecord srcNode, Offer offer) {
    try {
      if (offer.getContentKeys().isEmpty()) return new Accept(0, Bytes.EMPTY);
      byte[] contentKeysBitArray = new byte[offer.getContentKeys().size()];
      List<Bytes> contentKeyAccepted = new ArrayList<>();
      for (int x = 0; x < offer.getContentKeys().size(); x++) {
        Bytes contentKey = offer.getContentKeys().get(x);

        //      double d = distance(cid, nodeId); // Example: implement the 'distance' method
        //      if (d >= nodeRadius) {
        //        log("OFFER", "Content key: " + toHexString(cid) + " is outside radius.\nd=" + d +
        // "\nr=" + nodeRadius);
        //        continue;
        //      }
        if (this.historyDB.get(ContentKey.decode(contentKey)).isEmpty()) {
          contentKeysBitArray[x] = 1;
          contentKeyAccepted.add(contentKey);
        }
      }
      if (contentKeyAccepted.isEmpty()) return new Accept(0, Bytes.of(contentKeysBitArray));

      int connectionId =
          this.utpManager.acceptRead(
              srcNode,
              (newContent) -> {
                if (newContent.size() == contentKeyAccepted.size()) {
                  for (int i = 0; i < newContent.size(); i++) {
                    this.historyDB.saveContent(contentKeyAccepted.get(i), newContent.get(i));
                  }
                }
              });
      return new Accept(connectionId, Bytes.of(contentKeysBitArray));
    } catch (Exception e) {
      LOG.trace("Error when handling Offer Message");
      return new Accept(0, Bytes.EMPTY);
    }
  }

  private org.apache.tuweni.units.bigints.UInt64 getLocalEnrSeq() {
    return discv5Client.getEnrSeq();
  }

  @Override
  public NetworkType getNetworkType() {
    return networkType;
  }

  @Override
  public CompletableFuture<Void> checkLiveness(NodeRecord nodeRecord) {
    LOG.info("checkLiveness");
    Ping pingMessage =
        new Ping(
            nodeRecord.getSeq(),
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            new ClientInfoAndCapabilities(this.nodeRadius).getSszBytes());
    return CompletableFuture.supplyAsync(() -> this.ping(nodeRecord, pingMessage))
        .thenCompose((__) -> new CompletableFuture<>());
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
      LOG.trace("Something when wrong when sending a {} with error {}", message, error);
      return SafeFuture.completedFuture(Optional.empty());
    };
  }

  // TODO THIS MUST BE REFACTORED.
  public Optional<Bytes> getContent(ContentKey contentKey) {
    return this.historyDB.get(contentKey);
  }
}
