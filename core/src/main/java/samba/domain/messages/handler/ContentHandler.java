package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.response.Content;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class ContentHandler implements PortalWireMessageHandler<Content> {
    
    @Override
    public Optional<PortalWireMessage> handle(Content message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}