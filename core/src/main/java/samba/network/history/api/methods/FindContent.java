package samba.network.history.api.methods;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.api.jsonrpc.results.FindContentResult;
import samba.network.history.api.HistoryNetworkInternalAPI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FindContent {

    private static final Logger LOG = LoggerFactory.getLogger(FindContent.class);

    private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

    public FindContent(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
        this.historyNetworkInternalAPI = historyNetworkInternalAPI;
    }

    private Optional<FindContentResult> execute(final String enr, final Bytes contentKey) {
        try {
            return this.historyNetworkInternalAPI
                    .findContent(NodeRecordFactory.DEFAULT.fromEnr(enr),
                            new samba.domain.messages.requests.FindContent(contentKey)).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Error when executing FindContent operation");
            return Optional.empty();
        }
    }

    public static Optional<FindContentResult> execute(
            final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String enr, final Bytes contentKey) {
        LOG.debug("Executing FindContent with parameters enr:{} and contentKey {}", enr, contentKey);
        return new FindContent(historyNetworkInternalAPI).execute(enr, contentKey);
    }
}
