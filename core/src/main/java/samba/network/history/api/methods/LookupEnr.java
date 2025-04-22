package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookupEnr {

  private static final Logger LOG = LoggerFactory.getLogger(LookupEnr.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public LookupEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<String> execute(final String nodeId) {
    return this.historyNetworkInternalAPI.lookupEnr(UInt256.fromHexString(nodeId));
  }

  public static Optional<String> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String nodeId) {
    LOG.debug("Executing LookupEnr with parameters nodeId {}", nodeId);
    return new LookupEnr(historyNetworkInternalAPI).execute(nodeId);
  }
}
