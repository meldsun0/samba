package samba.domain.messages.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.w3c.dom.Node;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.handler.AcceptHandler;
import samba.domain.messages.handler.ContentHandler;
import samba.domain.messages.handler.FindContentHandler;
import samba.domain.messages.handler.FindNodesHandler;
import samba.domain.messages.handler.NodesHandler;
import samba.domain.messages.handler.OfferHandler;
import samba.domain.messages.handler.PingHandler;
import samba.domain.messages.handler.PongHandler;
import samba.domain.messages.handler.PortalWireMessageHandler;

public class PortalWireMessageProcessor {
    private final Map<MessageType, PortalWireMessageHandler> messageHandlers = new HashMap<>();

    public PortalWireMessageProcessor() {
        messageHandlers.put(MessageType.PING, new PingHandler());
        messageHandlers.put(MessageType.PONG, new PongHandler());
        messageHandlers.put(MessageType.FIND_NODES, new FindNodesHandler());
        messageHandlers.put(MessageType.NODES, new NodesHandler());
        messageHandlers.put(MessageType.FIND_CONTENT, new FindContentHandler());
        messageHandlers.put(MessageType.CONTENT, new ContentHandler());
        messageHandlers.put(MessageType.OFFER, new OfferHandler());
        messageHandlers.put(MessageType.ACCEPT, new AcceptHandler());
    }

    public Optional<PortalWireMessage> handleMessage(PortalWireMessage message, NodeRecord srcNode) {
        PortalWireMessageHandler handler = messageHandlers.get(message.getMessageType());
        if (handler != null) {
            Optional<PortalWireMessage> handlerResponse = handler.handle(message, srcNode);
            return handlerResponse;
        }
        return Optional.empty();

    }
}