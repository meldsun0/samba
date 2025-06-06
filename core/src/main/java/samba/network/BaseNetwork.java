package samba.network;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.PortalWireMessageDecoder;
import samba.network.exception.BadRequestException;
import samba.network.exception.MessageToOurselfException;
import samba.network.exception.NoSupportedProtocolVersionException;
import samba.network.exception.StoreNotAvailableException;
import samba.services.discovery.Discv5Client;
import samba.util.ProtocolVersionUtil;

import java.util.Optional;

import com.google.common.base.Throwables;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public abstract class BaseNetwork implements Network {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  protected NetworkType networkType;
  protected Discv5Client discv5Client;
  protected UInt256 nodeRadius;

  public BaseNetwork(NetworkType networkType, Discv5Client discv5Client, UInt256 nodeRadius) {
    this.networkType = networkType;
    this.discv5Client = discv5Client;
    this.nodeRadius = nodeRadius;
  }

  protected SafeFuture<Optional<PortalWireMessage>> sendMessage(
      NodeRecord destinationNode, PortalWireMessage messageRequest) {
    LOG.debug(
        "Send Portal {} message to {}",
        messageRequest.getMessageType(),
        destinationNode.getNodeId());
    if (!isStoreAvailable()) {
      return SafeFuture.failedFuture(new StoreNotAvailableException());
    }
    if (isOurself(destinationNode)) {
      return SafeFuture.failedFuture(new MessageToOurselfException());
    }
    Optional<Integer> protocolVersion =
        ProtocolVersionUtil.getHighestSupportedProtocolVersion(
            ProtocolVersionUtil.getSupportedProtocolVersions(destinationNode));
    if (protocolVersion.isEmpty()) {
      return SafeFuture.failedFuture(new NoSupportedProtocolVersionException());
    }

    // TODO FIX chain order
    return SafeFuture.of(
            discv5Client
                .sendDiscv5Message(
                    destinationNode, this.networkType.getValue(), messageRequest.getSszBytes())
                .thenApply(
                    (sszbytes) ->
                        parseResponse(
                            sszbytes,
                            destinationNode,
                            messageRequest,
                            protocolVersion.get())) // Change
                .thenApply(Optional::of))
        .thenPeek(this::logResponse)
        .exceptionallyCompose(error -> handleSendMessageError(messageRequest, error));
  }

  private boolean isOurself(NodeRecord node) {
    return this.discv5Client.getNodeId().isPresent()
        && this.discv5Client.getNodeId().get().equals(node.getNodeId());
  }

  protected abstract boolean isStoreAvailable();

  private void logResponse(Optional<PortalWireMessage> portalWireMessage) {
    portalWireMessage.ifPresent(
        (message) -> LOG.debug("Portal {} message received", message.getMessageType()));
  }

  private SafeFuture<Optional<PortalWireMessage>> handleSendMessageError(
      PortalWireMessage message, Throwable error) {
    LOG.debug("Something when wrong when sending a Portal {} message", message.getMessageType());
    final Throwable rootCause = Throwables.getRootCause(error);
    if (rootCause instanceof IllegalArgumentException) {
      return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
    }
    return SafeFuture.failedFuture(error);
  }

  private PortalWireMessage parseResponse(
      Bytes sszbytes,
      NodeRecord destinationNode,
      PortalWireMessage requestMessage,
      int protocolVersion) {
    // TODO validate appropriate response. If I send a Ping I must get a PONG
    return PortalWireMessageDecoder.decode(destinationNode, sszbytes, protocolVersion);
  }
}
