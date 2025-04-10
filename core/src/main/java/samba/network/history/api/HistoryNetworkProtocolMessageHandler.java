package samba.network.history.api;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.network.NetworkType;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface HistoryNetworkProtocolMessageHandler {

  PortalWireMessage handlePing(NodeRecord srcNode, Ping ping);

  PortalWireMessage handleFindNodes(NodeRecord srcNode, FindNodes findNodes);

  PortalWireMessage handleFindContent(NodeRecord srcNode, FindContent findContent);

  PortalWireMessage handleOffer(NodeRecord srcNode, Offer offer);

  NetworkType getNetworkType();
}
