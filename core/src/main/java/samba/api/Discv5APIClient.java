package samba.api;

import samba.api.jsonrpc.results.NodeInfo;
import samba.network.history.api.methods.Discv5FindNode;
import samba.network.history.api.methods.Discv5GetEnr;
import samba.network.history.api.methods.Discv5NodeInfo;
import samba.network.history.api.methods.Discv5UpdateNodeInfo;
import samba.services.discovery.Discv5Client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Discv5APIClient implements Discv5API {

  private final Discv5Client discv5Client;

  public Discv5APIClient(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  @Override
  public Optional<String> getEnr(String nodeId) {
    return Discv5GetEnr.execute(this.discv5Client, nodeId);
  }

  @Override
  public Optional<NodeInfo> getNodeInfo() {
    return Discv5NodeInfo.execute(this.discv5Client);
  }

  @Override
  public Optional<NodeInfo> updateNodeInfo(InetSocketAddress socketAddress, boolean isTCP) {
    return Discv5UpdateNodeInfo.execute(this.discv5Client, socketAddress, isTCP);
  }

  @Override
  public Optional<List<String>> findNodes(String enr, Set<Integer> distances) {
    return Discv5FindNode.execute(this.discv5Client, enr, distances);
  }
}
