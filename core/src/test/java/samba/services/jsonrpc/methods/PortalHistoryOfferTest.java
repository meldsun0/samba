package samba.services.jsonrpc.methods;

import org.junit.jupiter.api.BeforeEach;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.history.PortalHistoryPing;

import static org.mockito.Mockito.mock;

public class PortalHistoryOffer {

    private final String JSON_RPC_VERSION = "2.0";
    private final String PORTAL_HISTORY_OFFER = "portal_historyOffer";
    private PortalHistoryPing method;
    private Discv5Client discv5Client;
    private HistoryNetwork historyJsonRpc;

    @BeforeEach
    public void before() {
        this.historyJsonRpc = mock(HistoryNetwork.class);
        this.discv5Client = mock(Discv5Client.class);
        method = new PortalHistoryOffer(this.historyJsonRpc, this.discv5Client);
    }
}
