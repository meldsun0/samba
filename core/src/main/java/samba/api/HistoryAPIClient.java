package samba.api;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.NodeInfo;
import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;
import samba.network.history.api.HistoryNetworkInternalAPI;
import samba.network.history.api.methods.AddEnr;
import samba.network.history.api.methods.DeleteEnr;
import samba.network.history.api.methods.Discv5GetEnr;
import samba.network.history.api.methods.Discv5NodeInfo;
import samba.network.history.api.methods.Discv5UpdateNodeInfo;
import samba.network.history.api.methods.FindContent;
import samba.network.history.api.methods.FindNodes;
import samba.network.history.api.methods.GetEnr;
import samba.network.history.api.methods.GetLocalContent;
import samba.network.history.api.methods.LookupEnr;
import samba.network.history.api.methods.Offer;
import samba.network.history.api.methods.PutContent;
import samba.network.history.api.methods.Store;
import samba.services.discovery.Discv5Client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;

public class HistoryAPIClient implements HistoryAPI {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;


  public HistoryAPIClient(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;

  }

  @Override
  public PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue) {
    return PutContent.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
  }

  @Override
  public boolean store(Bytes contentKey, Bytes contentValue) {
    return Store.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
  }

  @Override
  public Optional<String> getEnr(String nodeId) {
    return GetEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public boolean deleteEnr(String nodeId) {
    return DeleteEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public boolean addEnr(String enr) {
    return AddEnr.execute(this.historyNetworkInternalAPI, enr);
  }

  @Override
  public Optional<FindContentResult> findContent(String enr, Bytes contentKey) {
    return FindContent.execute(this.historyNetworkInternalAPI, enr, contentKey);
  }

  @Override
  public List<String> findNodes(String enr, Set<Integer> distances) {
    return FindNodes.execute(this.historyNetworkInternalAPI, enr, distances);
  }

  @Override
  public Optional<String> getLocalContent(Bytes contentKey) {
    return GetLocalContent.execute(this.historyNetworkInternalAPI, contentKey);
  }

  @Override
  public Optional<String> lookupEnr(String nodeId) {
    return LookupEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public Optional<Bytes> offer(String enr, List<Bytes> contents, List<Bytes> contentKeys) {
    return Offer.execute(this.historyNetworkInternalAPI, enr, contents, contentKeys);
  }
}
