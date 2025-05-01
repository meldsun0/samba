package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

public class TraceGetContent {

  final HistoryNetworkInternalAPI historyNetworkInternalAPI;
  final int SEARCH_TIMEOUT = 60;

  public TraceGetContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }
}
