package samba.domain.messages.handler;

import samba.domain.messages.requests.Offer;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.Network;
import samba.network.history.HistoryNetworkIncomingRequests;

public class OfferHandler implements PortalWireMessageHandler<Offer> {
    
    @Override
    public void handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, Offer offer) {
        //TODO implement OfferHandler
    }
}