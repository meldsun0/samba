package samba.domain.messages.handler;

import samba.domain.messages.PortalWireMessage;
import samba.network.history.HistoryNetworkIncomingRequests;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface PortalWireMessageHandler<Message> {

  PortalWireMessage handle(
      HistoryNetworkIncomingRequests network, NodeRecord srcNode, Message message);
}
