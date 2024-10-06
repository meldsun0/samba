package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.response.Pong;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class PongHandler implements PortalWireMessageHandler<Pong> {
    
    @Override
    public Optional<PortalWireMessage> handle(Pong message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}