package samba.api.libary;

import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;
import samba.network.history.api.HistoryNetworkInternalAPI;
import samba.network.history.api.methods.AddEnr;
import samba.network.history.api.methods.DeleteEnr;
import samba.network.history.api.methods.GetEnr;
import samba.network.history.api.methods.PutContent;
import samba.network.history.api.methods.Store;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public class HistoryLibraryAPIImpl implements HistoryLibraryAPI {

    private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

    public HistoryLibraryAPIImpl(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
        this.historyNetworkInternalAPI = historyNetworkInternalAPI;
    }

    @Override
    public PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue) {
        return PutContent.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
    }

    @Override
    public boolean store(Bytes contentKey, Bytes contentValue) {
        return Store.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
    }

    @Override
    public Optional<String> getEnr(String nodeId) {
        return GetEnr.execute(this.historyNetworkInternalAPI, nodeId);
    }

    @Override
    public boolean deleteEnr(String nodeId) {
        return DeleteEnr.execute(this.historyNetworkInternalAPI, nodeId);
    }

    @Override
    public boolean addEnr(String enr) {
        return AddEnr.execute(this.historyNetworkInternalAPI, enr);
    }
}
