package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.network.NetworkType;

public interface HistoryNetworkIncomingRequests {

    PortalWireMessage handlePing(NodeRecord srcNode, Ping ping);

    PortalWireMessage handleFindNodes(NodeRecord srcNode, FindNodes findNodes);

    PortalWireMessage handleFindContent(NodeRecord srcNode, FindContent findContent);

    PortalWireMessage handleOffer(NodeRecord srcNode, Offer offer);

    NetworkType getNetworkType();
}
