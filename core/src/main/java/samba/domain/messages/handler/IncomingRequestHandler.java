package samba.domain.messages.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.PortalWireMessageDecoder;

public class IncomingRequestHandler implements TalkHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingRequestHandler.class);
    private final Map<MessageType, PortalWireMessageHandler> messageHandlers = new HashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public IncomingRequestHandler build() {
        started.set(true);
        return this;
    }

    public IncomingRequestHandler addHandler(MessageType messageType, PortalWireMessageHandler handler) {
        //TODO add validations.
        if (started.get()) {
            throw new RuntimeException("IncomingRequestProcessor already started, couldn't add any handlers");
        }
        this.messageHandlers.put(messageType, handler);
        return this;
    }

    @Override
    public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
        LOG.info("TALKREQ message received");
        //TODO - Validate protocol
        PortalWireMessage message = PortalWireMessageDecoder.decode(srcNode, request);
        PortalWireMessageHandler handler = messageHandlers.get(message.getMessageType());
        if (handler != null) {
            handler.handle(srcNode, message);
        }//NODES, CONTENT, ACCEPT, PONG
        return CompletableFuture.completedFuture(Bytes.EMPTY);
    }

}