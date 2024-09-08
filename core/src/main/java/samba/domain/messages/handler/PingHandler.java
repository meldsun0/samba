package samba.domain.messages.handler;

import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.Ping;
import samba.domain.messages.PortalWireMessage;

public class PingHandler implements PortalWireMessageHandler<Ping> {
    
    @Override
    public Optional<PortalWireMessage> handle(Ping message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}