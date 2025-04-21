package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Store {

  private static final Logger LOG = LoggerFactory.getLogger(Store.class);
  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public Store(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private boolean execute(final Bytes contentKey, final Bytes contentValue) {
    return this.historyNetworkInternalAPI.store(contentKey, contentValue);
  }

  public static boolean execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI,
      final Bytes contentKey,
      final Bytes contentValue) {
    LOG.debug(
        "Executing Store with parameters contentKey:{} contentValue:{}", contentKey, contentValue);
    return new Store(historyNetworkInternalAPI).execute(contentKey, contentValue);
  }
}
