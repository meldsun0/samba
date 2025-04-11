package samba.domain.messages.handler;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.Offer;
import samba.network.history.api.HistoryNetworkProtocolMessageHandler;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfferHandler implements PortalWireMessageHandler<Offer> {

  private static final Logger LOG = LoggerFactory.getLogger(OfferHandler.class);

  @Override
  public PortalWireMessage handle(
      HistoryNetworkProtocolMessageHandler network, NodeRecord srcNode, Offer offer) {
    LOG.info("{} message received", offer.getMessageType());
    PortalWireMessage accept = network.handleOffer(srcNode, offer);
    return accept;
  }
}
