package samba.network.history.api.methods;

import samba.api.jsonrpc.results.NodeInfo;
import samba.services.discovery.Discv5Client;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5UpdateNodeInfo {
  private static final Logger LOG = LoggerFactory.getLogger(Discv5UpdateNodeInfo.class);

  private final Discv5Client discv5Client;

  public Discv5UpdateNodeInfo(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<NodeInfo> execute(final InetSocketAddress socketAddress, boolean isTCP) {
    boolean result = this.discv5Client.updateEnrSocket(socketAddress, isTCP);
    if (result) {
      NodeRecord newNodeRecord = this.discv5Client.getHomeNodeRecord();
      return Optional.of(
          new NodeInfo(newNodeRecord.asEnr(), newNodeRecord.getNodeId().toHexString()));
    }
    return Optional.empty();
  }

  public static Optional<NodeInfo> execute(
      final Discv5Client discv5Client, final InetSocketAddress socketAddress, boolean isTCP) {
    LOG.debug(
        "Executing Discv5UpdateNodeInfo with parameters socketAddress {}, isTCP {}",
        socketAddress,
        isTCP);
    return new Discv5UpdateNodeInfo(discv5Client).execute(socketAddress, isTCP);
  }
}
