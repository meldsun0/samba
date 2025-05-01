package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5AddEnr {
  private static final Logger LOG = LoggerFactory.getLogger(Discv5AddEnr.class);

  private final Discv5Client discv5Client;

  public Discv5AddEnr(Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private boolean execute(String enr) {
    return this.discv5Client.addEnr(enr);
  }

  public static boolean execute(final Discv5Client discv5Client, final String enr) {
    LOG.debug("Executing Discv5AddEnr with parameters enr:{}", enr);
    return new Discv5AddEnr(discv5Client).execute(enr);
  }
}
