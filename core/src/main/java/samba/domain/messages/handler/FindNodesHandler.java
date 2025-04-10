package samba.domain.messages.handler;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindNodes;
import samba.network.history.api.HistoryNetworkProtocolMessageHandler;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindNodesHandler implements PortalWireMessageHandler<FindNodes> {

  private static final Logger LOG = LoggerFactory.getLogger(FindNodesHandler.class);

  @Override
  public PortalWireMessage handle(
          HistoryNetworkProtocolMessageHandler network, NodeRecord srcNode, FindNodes findNodes) {
    LOG.info("{} message received", findNodes.getMessageType());
    return network.handleFindNodes(srcNode, findNodes);
  }
}
