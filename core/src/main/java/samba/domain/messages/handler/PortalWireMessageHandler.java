package samba.domain.messages.handler;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.network.history.HistoryNetworkIncomingRequests;

public interface PortalWireMessageHandler<Message> {
    
    PortalWireMessage handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, Message message);
}