package samba.domain.messages.handler;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.IncomingRequestHandler;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.PortalWireMessageDecoder;
import samba.network.NetworkType;
import samba.network.history.HistoryNetworkIncomingRequests;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryNetworkIncomingRequestHandler implements IncomingRequestHandler {

  private static final Logger LOG =
      LoggerFactory.getLogger(HistoryNetworkIncomingRequestHandler.class);
  private final Map<MessageType, PortalWireMessageHandler> historyMessageHandlers = new HashMap<>();

  private HistoryNetworkIncomingRequests network;

  public HistoryNetworkIncomingRequestHandler(final HistoryNetworkIncomingRequests network) {
    this.network = network;
  }

  public HistoryNetworkIncomingRequestHandler addHandler(
      MessageType messageType, PortalWireMessageHandler handler) {
    this.historyMessageHandlers.put(messageType, handler);
    return this;
  }

  @Override
  public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
    checkArgument(
        this.network.getNetworkType().isEquals(protocol),
        "TALKKREQ message is not from the {}",
        this.network.getNetworkType().getName());

    PortalWireMessage message = PortalWireMessageDecoder.decode(srcNode, request);
    PortalWireMessageHandler handler = historyMessageHandlers.get(message.getMessageType());
    Bytes response = Bytes.EMPTY;
    if (handler != null) {
      PortalWireMessage responsePacket = handler.handle(this.network, srcNode, message);
      response = responsePacket.getSszBytes();
    } else {
      LOG.info(
          "{} message not expected in TALKREQ",
          message.getMessageType()); // NODES, CONTENT, ACCEPT, PONG
    }
    return CompletableFuture.completedFuture(response);
  }

  @Override
  public NetworkType getNetworkType() {
    return this.network.getNetworkType();
  }
}
