package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class FindNodesHandler implements PortalWireMessageHandler<FindNodes> {
    
    @Override
    public Optional<PortalWireMessage> handle(FindNodes message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}