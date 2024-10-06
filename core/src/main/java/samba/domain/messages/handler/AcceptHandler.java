package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.response.Accept;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class AcceptHandler implements PortalWireMessageHandler<Accept> {
    
    @Override
    public Optional<PortalWireMessage> handle(Accept message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}