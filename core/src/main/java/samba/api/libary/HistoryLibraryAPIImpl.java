package samba.api.libary;

import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;
import samba.network.history.api.HistoryNetworkInternalAPI;
import samba.network.history.api.methods.PutContent;

import org.apache.tuweni.bytes.Bytes;

public class HistoryLibraryAPIImpl implements HistoryLibraryAPI {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public HistoryLibraryAPIImpl(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue) {
    return PutContent.execute(historyNetworkInternalAPI, contentKey, contentValue);
  }
}
