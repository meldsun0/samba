package samba.domain.messages.handler;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.network.history.HistoryNetworkIncomingRequests;

public class FindContentHandler implements PortalWireMessageHandler<FindContent> {

    @Override
    public PortalWireMessage handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, FindContent findContent) {
        //TODO implement FindContentHandler
        return null;
    }
}