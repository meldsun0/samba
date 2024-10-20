package samba.domain.messages.handler;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindNodes;
import samba.network.history.HistoryNetworkIncomingRequests;

public class FindNodesHandler implements PortalWireMessageHandler<FindNodes> {

    @Override
    public PortalWireMessage handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, FindNodes findNodes) {
        //TODO implement FindNodeHandler
        return null;
    }
}