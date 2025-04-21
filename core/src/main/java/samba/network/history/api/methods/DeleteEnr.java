package samba.network.history.api.methods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.network.history.api.HistoryNetworkInternalAPI;

public class DeleteEnr {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteEnr.class);

    private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

    public DeleteEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
        this.historyNetworkInternalAPI = historyNetworkInternalAPI;
    }

    private boolean execute(String nodeId) {
        return this.historyNetworkInternalAPI.deleteEnr(nodeId);
    }

    public static boolean execute(
            final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String nodeId) {
        LOG.debug("Executing DeleteEnr with parameters nodeId:{}", nodeId);
        return new DeleteEnr(historyNetworkInternalAPI).execute(nodeId);
    }
}
