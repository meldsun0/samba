package samba.domain.messages.handler;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.Offer;
import samba.network.history.HistoryNetworkIncomingRequests;

public class OfferHandler implements PortalWireMessageHandler<Offer> {
    
    @Override
    public PortalWireMessage handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, Offer offer) {
        //TODO implement OfferHandler
        return null;
    }
}