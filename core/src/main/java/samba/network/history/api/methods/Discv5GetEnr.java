package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5GetEnr {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5GetEnr.class);

  private final Discv5Client discv5Client;

  public Discv5GetEnr(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<String> execute(String nodeId) {
    return discv5Client.lookupEnr(UInt256.fromHexString(nodeId));
  }

  public static Optional<String> execute(final Discv5Client discv5Client, final String nodeId) {
    LOG.debug("Executing Discv5GetEnr with parameter nodeId:{}", nodeId);
    return new Discv5GetEnr(discv5Client).execute(nodeId);
  }
}
