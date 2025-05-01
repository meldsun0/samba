package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5DeleteEnr {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5DeleteEnr.class);

  private final Discv5Client discv5Client;

  public Discv5DeleteEnr(Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private boolean execute(String nodeId) {
    return this.discv5Client.deleteEnr(nodeId);
  }

  public static boolean execute(Discv5Client discv5Client, String nodeId) {
    LOG.debug("Executing Discv5DeleteEnr with parameters nodeId:{}", nodeId);
    return new Discv5DeleteEnr(discv5Client).execute(nodeId);
  }
}
