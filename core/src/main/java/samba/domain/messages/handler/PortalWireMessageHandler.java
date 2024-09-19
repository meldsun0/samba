package samba.domain.messages.handler;

import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;

public interface PortalWireMessageHandler<Message> {
    
    Optional<PortalWireMessage> handle(Message message, NodeRecord srcNode);
}