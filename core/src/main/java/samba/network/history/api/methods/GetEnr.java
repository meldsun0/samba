package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetEnr {

  private static final Logger LOG = LoggerFactory.getLogger(GetEnr.class);
  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<String> execute(String nodeId) {
    return this.historyNetworkInternalAPI.getEnr(nodeId);
  }

  public static Optional<String> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String nodeId) {
    LOG.debug("Executing GetEnr with parameters nodeId:{}", nodeId);
    return new GetEnr(historyNetworkInternalAPI).execute(nodeId);
  }
}
