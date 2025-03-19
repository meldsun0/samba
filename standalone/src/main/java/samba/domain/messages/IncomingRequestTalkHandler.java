package samba.domain.messages;

import static com.google.common.base.Preconditions.checkNotNull;

import samba.domain.messages.handler.HistoryNetworkIncomingRequestHandler;
import samba.domain.messages.utp.UTPNetworkIncomingRequestHandler;
import samba.network.NetworkType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncomingRequestTalkHandler implements TalkHandler {

  private static final Logger LOG = LoggerFactory.getLogger(IncomingRequestTalkHandler.class);

  private HistoryNetworkIncomingRequestHandler historyNetworkIncomingRequestHandler;
  private UTPNetworkIncomingRequestHandler utpNetworkIncomingRequestHandler;

  private final AtomicBoolean started = new AtomicBoolean(false);

  public void start() {
    checkNotNull(
        historyNetworkIncomingRequestHandler,
        "History network incoming request handler not defined");
    checkNotNull(
        utpNetworkIncomingRequestHandler, "UTP network incoming request handler not defined");
    started.set(true);
  }

  @Override
  public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
    checkNotNull(
        NetworkType.fromBytes(protocol),
        "TALKKREQ message is not defined for {}",
        protocol.toHexString());
    NetworkType networkType = NetworkType.fromBytes(protocol);
    return switch (networkType) {
      case NetworkType.EXECUTION_HISTORY_NETWORK ->
          this.historyNetworkIncomingRequestHandler.talk(srcNode, protocol, request);
      case NetworkType.UTP ->
          this.utpNetworkIncomingRequestHandler.talk(srcNode, protocol, request);
      default -> this.defaultResponse(networkType);
    };
  }

  private CompletableFuture<Bytes> defaultResponse(NetworkType networkType) {
    LOG.info("{} message not expected in TALKREQ", networkType.getName());
    Bytes response = Bytes.EMPTY;
    return CompletableFuture.completedFuture(response);
  }

  public void addHandlers(
      HistoryNetworkIncomingRequestHandler historyNetworkIncomingRequestHandler,
      UTPNetworkIncomingRequestHandler utpNetworkIncomingRequestHandler) {
    this.historyNetworkIncomingRequestHandler = historyNetworkIncomingRequestHandler;
    this.utpNetworkIncomingRequestHandler = utpNetworkIncomingRequestHandler;
  }
}
