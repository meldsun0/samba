package samba.domain.messages.handler;

import samba.domain.messages.PortalWireMessage;
import samba.network.history.api.HistoryNetworkProtocolMessageHandler;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface PortalWireMessageHandler<Message> {

  PortalWireMessage handle(
      HistoryNetworkProtocolMessageHandler network, NodeRecord srcNode, Message message);
}
