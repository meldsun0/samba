package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.response.Nodes;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class NodesHandler implements PortalWireMessageHandler<Nodes> {
    
    @Override
    public Optional<PortalWireMessage> handle(Nodes message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}