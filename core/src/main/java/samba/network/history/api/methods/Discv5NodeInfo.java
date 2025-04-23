package samba.network.history.api.methods;

import samba.api.jsonrpc.results.NodeInfo;
import samba.services.discovery.Discv5Client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5NodeInfo {
  private static final Logger LOG = LoggerFactory.getLogger(Discv5NodeInfo.class);

  private final Discv5Client discv5Client;

  public Discv5NodeInfo(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<NodeInfo> execute() {
    return Optional.ofNullable(this.discv5Client.getHomeNodeRecord())
        .map(node -> new NodeInfo(node.asEnr(), node.getNodeId().toHexString()));
  }

  public static Optional<NodeInfo> execute(final Discv5Client discv5Client) {
    LOG.debug("Executing Discv5NodeInfo");
    return new Discv5NodeInfo(discv5Client).execute();
  }
}
