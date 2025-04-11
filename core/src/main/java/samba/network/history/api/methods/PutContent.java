package samba.network.history.api.methods;

import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutContent {

  private static final Logger LOG = LoggerFactory.getLogger(PutContent.class);
  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PutContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  public PutContentResult execute(final ContentKey contentKey, final Bytes contentValue) {
    boolean storedLocally =
        this.historyNetworkInternalAPI.store(contentKey.getSszBytes(), contentValue);
    Set<NodeRecord> nodes =
        this.historyNetworkInternalAPI.getFoundNodes(
            contentKey, this.historyNetworkInternalAPI.getMaxGossipCount(), true);
    this.historyNetworkInternalAPI.gossip(nodes, contentKey.getSszBytes(), contentValue);
    return new PutContentResult(storedLocally, nodes.size());
  }

  public static PutContentResult execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI,
      final ContentKey contentKey,
      final Bytes contentValue) {
    LOG.debug(
        "Executing PutContent with parameters contentKey:{} contentValue:{}",
        contentKey,
        contentValue);
    return new PutContent(historyNetworkInternalAPI).execute(contentKey, contentValue);
  }
}
