package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Offer {

  private static final Logger LOG = LoggerFactory.getLogger(Offer.class);
  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public Offer(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<Bytes> execute(
      final String enr, final List<Bytes> contents, final List<Bytes> contentKeys) {
    try {
      final NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);
      return this.historyNetworkInternalAPI
          .offer(nodeRecord, contents, new samba.domain.messages.requests.Offer(contentKeys))
          .get();
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing Offer operation");
      return Optional.empty();
    }
  }

  public static Optional<Bytes> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI,
      final String enr,
      final List<Bytes> contents,
      final List<Bytes> contentKeys) {
    LOG.debug(
        "Executing Offer with parameters enr:{} contents:{}  contentKeys: {}",
        enr,
        contents,
        contentKeys);
    return new Offer(historyNetworkInternalAPI).execute(enr, contents, contentKeys);
  }
}
