package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.Offer;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class OfferHandler implements PortalWireMessageHandler<Offer> {
    
    @Override
    public Optional<PortalWireMessage> handle(Offer message, NodeRecord srcNode) {
        
        return Optional.empty();
    }
    
}