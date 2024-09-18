package samba.services.connecton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import samba.network.PeerClient;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

public class ConnectionManager extends Service {

    private static final Logger LOG = LogManager.getLogger();

    private ConnectionPool connectionPool;
    private PeerClient client;




    @Override
    protected SafeFuture<?> doStart() {
        LOG.info("Starting connection manager");
        createRecurrentSearchTask();
        return SafeFuture.COMPLETE;
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }
    private void createRecurrentSearchTask() {
        searchForPeers().alwaysRun(this::createNextSearchPeerTask).finish(this::logSearchError);
    }


    private SafeFuture<Void> searchForPeers() {
        if (!isRunning()) {
            LOG.trace("Not running so not searching for peers");
            return SafeFuture.COMPLETE;
        }
        LOG.trace("Searching for peers");
        return client.
                .searchForPeers()
                .orTimeout(30, TimeUnit.SECONDS)
                .handle(
                        (peers, error) -> {
                            if (error == null) {
                                connectToBestPeers(peers);
                            } else {
                                LOG.debug("Discovery failed", error);
                                connectToBestPeers(emptyList());
                            }
                            return null;
                        });
    }
}
