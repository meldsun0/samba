package samba.domain.messages.handler;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.network.history.api.HistoryNetworkProtocolMessageHandler;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindContentHandler implements PortalWireMessageHandler<FindContent> {

  private static final Logger LOG = LoggerFactory.getLogger(FindContentHandler.class);

  @Override
  public PortalWireMessage handle(
      HistoryNetworkProtocolMessageHandler network, NodeRecord srcNode, FindContent findContent) {
    LOG.info("{} message received", findContent.getMessageType());
    PortalWireMessage content = network.handleFindContent(srcNode, findContent);
    return content;
  }
}
