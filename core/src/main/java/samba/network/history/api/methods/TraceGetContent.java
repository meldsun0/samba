package samba.network.history.api.methods;

import samba.api.jsonrpc.results.TraceGetContentResult;
import samba.api.jsonrpc.schemas.TraceResultObjectJson;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;

public class TraceGetContent {

  final HistoryNetworkInternalAPI historyNetworkInternalAPI;
  final int SEARCH_TIMEOUT = 60;

  public TraceGetContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<TraceGetContentResult> execute(final Bytes contentKeyInBytes) {

    long startTime = System.currentTimeMillis();
    UInt256 localNodeId = this.historyNetworkInternalAPI.getLocalNodeId();
    UInt256 contentKeyinUInt256 = UInt256.fromBytes(contentKeyInBytes);

    ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyInBytes).get();
    Optional<String> content = this.historyNetworkInternalAPI.getLocalContent(contentKey);

    if (content.isPresent()) {
      TraceResultObjectJson traceResult =
          new TraceResultObjectJson(
              localNodeId,
              contentKeyinUInt256,
              localNodeId,
              new HashMap<>(),
              new HashMap<>(),
              startTime,
              List.of());
      return Optional.of(new TraceGetContentResult(content.get(), false, traceResult));
    } else {
      Optional<TraceGetContentResult> traceGetContentResult =
          this.historyNetworkInternalAPI.traceGetContent(contentKey, SEARCH_TIMEOUT, startTime);
      if (traceGetContentResult.isPresent() && traceGetContentResult.get().getContent() != null) {
        this.historyNetworkInternalAPI.store(
            contentKey.getSszBytes(),
            Bytes.fromHexString(traceGetContentResult.get().getContent()));
        return traceGetContentResult;
      } else {
        return Optional.empty();
      }
    }
  }

  public static Optional<TraceGetContentResult> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Bytes contentKey) {
    return new TraceGetContent(historyNetworkInternalAPI).execute(contentKey);
  }
}
