package samba.network.history.api.methods;

import samba.api.jsonrpc.results.PutContentResult;
import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private PutContentResult execute(final Bytes contentKeyInBytes, final Bytes contentValue) {
    ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyInBytes).get();
    boolean storedLocally =
        this.historyNetworkInternalAPI.store(contentKey.getSszBytes(), contentValue);
    Set<NodeRecord> nodes =
        this.historyNetworkInternalAPI.getFoundNodes(
            contentKey, this.historyNetworkInternalAPI.getMaxGossipCount(), true);
    if (nodes.size() < this.historyNetworkInternalAPI.getMaxGossipCount()) {
      Optional<RecursiveFindNodesResult> newNodes =
          this.historyNetworkInternalAPI.recursiveFindNodes(null, nodes, 0);
      if (newNodes.isPresent()) {
        nodes.addAll(
            Stream.of(newNodes.get())
                .flatMap(result -> result.getNodes().stream())
                .map(enr -> this.historyNetworkInternalAPI.nodeRecordFromEnr(enr))
                .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                .limit(this.historyNetworkInternalAPI.getMaxGossipCount() - nodes.size())
                .collect(Collectors.toSet()));
      }
    }
    this.historyNetworkInternalAPI.gossip(nodes, contentKey.getSszBytes(), contentValue);
    return new PutContentResult(storedLocally, nodes.size());
  }

  public static PutContentResult execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI,
      final Bytes contentKey,
      final Bytes contentValue) {
    LOG.debug(
        "Executing PutContent with parameters contentKey:{} contentValue:{}",
        contentKey,
        contentValue);
    return new PutContent(historyNetworkInternalAPI).execute(contentKey, contentValue);
  }
}
