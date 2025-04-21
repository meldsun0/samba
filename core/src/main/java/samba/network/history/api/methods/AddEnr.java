package samba.network.history.api.methods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.network.history.api.HistoryNetworkInternalAPI;

public class AddEnr {
    private static final Logger LOG = LoggerFactory.getLogger(AddEnr.class);

    private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

    public AddEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
        this.historyNetworkInternalAPI = historyNetworkInternalAPI;
    }

    private boolean execute(String enr) {
        return this.historyNetworkInternalAPI.addEnr(enr);
    }

    public static boolean execute(
            final HistoryNetworkInternalAPI historyNetworkInternalAPI, final String enr) {
        LOG.debug("Executing AddEnr with parameters enr:{}", enr);
        return new AddEnr(historyNetworkInternalAPI).execute(enr);
    }
}
