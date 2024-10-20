package samba.domain.messages.handler;

import samba.domain.messages.requests.FindContent;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.Network;
import samba.network.history.HistoryNetworkIncomingRequests;

public class FindContentHandler implements PortalWireMessageHandler<FindContent> {

    @Override
    public void handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, FindContent findContent) {
        //TODO implement FindContentHandler
    }
}