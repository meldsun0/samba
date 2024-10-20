package samba.domain.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.handler.PortalWireMessageHandler;
import samba.network.history.HistoryNetworkIncomingRequests;

public class IncomingRequestHandler implements TalkHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingRequestHandler.class);

    private final Map<MessageType, PortalWireMessageHandler> messageHandlers = new HashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private HistoryNetworkIncomingRequests network;


    public IncomingRequestHandler build(HistoryNetworkIncomingRequests network) {
        started.set(true);
        this.network = network;
        return this;
    }

    public IncomingRequestHandler addHandler(MessageType messageType, PortalWireMessageHandler handler) {
        if (started.get()) {
            throw new RuntimeException("IncomingRequestProcessor already started, couldn't add any handlers");
        }
        this.messageHandlers.put(messageType, handler);
        return this;
    }

    @Override
    public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
        checkArgument(this.network.getNetworkType().isEquals(protocol),
                "TALKKREQ message is not from the {}", this.network.getNetworkType().getName());

        PortalWireMessage message = PortalWireMessageDecoder.decode(srcNode, request);
        PortalWireMessageHandler handler = messageHandlers.get(message.getMessageType());
        Bytes response = Bytes.EMPTY;
        if (handler != null) {
            PortalWireMessage responsePacket = handler.handle(this.network, srcNode, message);
            response = responsePacket.getSszBytes();
        } else {
            LOG.info("{} message not expected in TALKREQ", message.getMessageType()); //NODES, CONTENT, ACCEPT, PONG
        }
        return CompletableFuture.completedFuture(response);
    }
}