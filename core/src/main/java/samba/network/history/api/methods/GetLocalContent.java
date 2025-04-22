package samba.network.history.api.methods;

import samba.domain.content.ContentKey;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLocalContent {

  private static final Logger LOG = LoggerFactory.getLogger(GetLocalContent.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetLocalContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<String> execute(final Bytes contentKey) {
    Optional<String> result =
        this.historyNetworkInternalAPI.getLocalContent(ContentKey.decode(contentKey));
    return result.map(value -> "0x00".equals(value) ? "0x" : value);
  }

  public static Optional<String> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Bytes contentKey) {
    LOG.debug("Executing GetLocalContent with parameters contentKey {}", contentKey);
    return new GetLocalContent(historyNetworkInternalAPI).execute(contentKey);
  }
}
