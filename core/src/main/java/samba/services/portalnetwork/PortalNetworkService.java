package samba.services.portalnetwork;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import samba.network.history.HistoryNetwork;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

import java.time.Duration;
import java.util.Optional;

public class PortalNetworkService  extends Service {

    private static final Logger LOG = LogManager.getLogger();


    private HistoryNetwork historyNetwork;
    private final AsyncRunner asyncRunner;

    public PortalNetworkService() {
        this.historyNetwork = new HistoryNetwork();

    }


    public PortalNetworkService(AsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
    }
    protected SafeFuture<?> doStart() {
        LOG.debug("Start {}", getClass().getSimpleName());
        return initialize().thenRun(this::fetchBlocks);
    }

    private SafeFuture<Void> initialize() {
        return null;
    }

    private void pingNode() {
        SafeFuture.asyncDoWhile(this::findPeerAndRequestBlocks)
                .always(
                        () -> {
                            if (isSyncDone()) {
                                stop().ifExceptionGetsHereRaiseABug();

                                reconstructHistoricalStatesService.ifPresent(
                                        service ->
                                                service
                                                        .start()
                                                        .finish(STATUS_LOG::reconstructHistoricalStatesServiceFailedStartup));
                            }
                        });
    }

    private void checkActiveNodes(){

        final Optional<MaxMissingBlockParams> blockParams = getMaxMissingBlockParams();
        if (blockParams.isPresent() && isActive() && requestInProgress.compareAndSet(false, true)) {
            return findPeer()
                    .map(peer -> requestBlocks(peer, blockParams.get()))
                    .orElseGet(this::waitToRetry)
                    .alwaysRun(() -> requestInProgress.set(false))
                    .thenApply(__ -> true);
        } else {
            return SafeFuture.completedFuture(false);
        }


         asyncRunner.runwit(() -> historyNetwork.ping(), Duration.ofMinutes(1));
    }





    @Override
    protected SafeFuture<?> doStart() {
        return null;
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }
}
