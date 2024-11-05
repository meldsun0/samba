package samba.domain.messages.handler;

import org.ethereum.beacon.discovery.message.NodesMessage;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindNodes;
import samba.network.history.HistoryNetworkIncomingRequests;

public class FindNodesHandler implements PortalWireMessageHandler<FindNodes> {

    private static final Logger LOG = LoggerFactory.getLogger(FindNodesHandler.class);

    @Override
    public PortalWireMessage handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, FindNodes findNodes) {
        LOG.info("{} message received", findNodes.getMessageType());
        return  network.handleFindNodes(srcNode, findNodes);
    }
}