package samba.domain.messages.handler;

import samba.domain.messages.requests.FindNodes;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.Network;
import samba.network.history.HistoryNetworkIncomingRequests;

public class FindNodesHandler implements PortalWireMessageHandler<FindNodes> {

    @Override
    public void handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, FindNodes findNodes) {
        //TODO implement FindNodeHandler
    }
}