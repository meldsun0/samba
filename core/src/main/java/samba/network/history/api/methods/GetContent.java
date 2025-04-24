package samba.network.history.api.methods;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.GetContentResult;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

//TODO refactor this

//  return result.map(value -> "0x00".equals(value) ? "0x" : value);
public class GetContent {

    private static final Logger LOG = LoggerFactory.getLogger(GetLocalContent.class);
    private final HistoryNetworkInternalAPI historyNetworkInternalAPI;
    private static final int SEARCH_TIMEOUT = 60;

    public GetContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
        this.historyNetworkInternalAPI = historyNetworkInternalAPI;
    }

    private Optional<GetContentResult> execute(final Bytes contentKeyInBytes) {

        ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyInBytes).get();
        Optional<String> content = this.historyNetworkInternalAPI.getLocalContent(contentKey);

        if (content.isPresent()) {
            return Optional.of(new GetContentResult(content.get(), false));
        } else {
            Optional<FindContentResult> findContentResult = this.historyNetworkInternalAPI.getContent(contentKey, SEARCH_TIMEOUT).join();
            if (findContentResult.isPresent() && findContentResult.get().getContent() != null) {
                this.historyNetworkInternalAPI.store(
                        contentKey.getSszBytes(), Bytes.fromHexString(findContentResult.get().getContent()));
                return
                        Optional.of(new GetContentResult(
                                findContentResult.get().getContent(), findContentResult.get().getUtpTransfer()));
            } else {
                return Optional.empty();
            }
        }
    }


    public static Optional<GetContentResult> execute(
            final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Bytes contentKey) {
        LOG.debug("Executing GetLocalContent with parameters contentKey {}", contentKey);
        return new GetContent(historyNetworkInternalAPI).execute(contentKey);
    }
}