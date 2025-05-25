package samba.network.history.api.methods;

import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveFindNodes {

  private static final Logger LOG = LoggerFactory.getLogger(RecursiveFindNodes.class);
  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;
  private static final int SEARCH_TIMEOUT = 60;

  public RecursiveFindNodes(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<RecursiveFindNodesResult> execute(final String nodeId) {
    Optional<RecursiveFindNodesResult> result =
        this.historyNetworkInternalAPI.recursiveFindNodes(nodeId, Set.of(), SEARCH_TIMEOUT);
    return result;
  }

  public static Optional<RecursiveFindNodesResult> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String nodeId) {
    LOG.debug("Executing RecursiveFindNodes with parameters nodeId {}", nodeId);
    return new RecursiveFindNodes(historyNetworkInternalAPI).execute(nodeId);
  }
}
