package samba.domain.messages.handler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.processor.PortalWireMessageDecoder;
import samba.domain.messages.processor.PortalWireMessageProcessor;



public class PortalDiscoveryMessageHandler implements TalkHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PortalDiscoveryMessageHandler.class);
    private final PortalWireMessageProcessor handler;

    public PortalDiscoveryMessageHandler(final PortalWireMessageProcessor handler) {
        this.handler = handler;
    }

    @Override
    public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
        PortalWireMessageDecoder decoder = new PortalWireMessageDecoder();
        // Decode Request
        PortalWireMessage message = decoder.decode(srcNode, request);
        Optional<PortalWireMessage> handlerResponse = handler.handleMessage(message, srcNode);
        if (handlerResponse.isPresent()) {
            PortalWireMessage response = handlerResponse.get();
            //response.serialize();
            // Return Response
            return CompletableFuture.completedFuture(Bytes.EMPTY);
        } else {
            return CompletableFuture.completedFuture(Bytes.EMPTY);
        }
    }
 
}