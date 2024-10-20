package samba.domain.messages.handler;

import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.PortalWireMessage;
import samba.network.Network;
import samba.network.history.HistoryNetwork;
import samba.network.history.HistoryNetworkIncomingRequests;

public interface PortalWireMessageHandler<Message> {
    
    void handle(HistoryNetworkIncomingRequests network, NodeRecord srcNode, Message message);
}