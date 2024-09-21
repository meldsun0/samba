package samba.services.connecton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import samba.metrics.SambaMetricCategory;
import samba.services.discovery.Discv5Client;
import samba.network.Network;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;


import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ConnectionService extends Service {

    private static final Logger LOG = LogManager.getLogger();
    protected static final Duration WARMUP_DISCOVERY_INTERVAL = Duration.ofSeconds(1);
    protected static final Duration DISCOVERY_INTERVAL = Duration.ofSeconds(30);

    private ConnectionPool connectionPool;
    private final Discv5Client discv5Client;
    private final Network network;

    private final Counter attemptedConnectionCounter;
    private final Counter successfulConnectionCounter;
    private final Counter failedConnectionCounter;
    private final AsyncRunner asyncRunner;
    private volatile Cancellable periodicPeerSearch;


    public ConnectionService(
            final MetricsSystem metricsSystem,
            final AsyncRunner asyncRunner,
            final Discv5Client discv5Client,
            final Network network) {

        this.asyncRunner = asyncRunner;
        this.network = network;
        this.discv5Client = discv5Client;

        final LabelledMetric<Counter> connectionAttemptCounter = metricsSystem.createLabelledCounter(SambaMetricCategory.NETWORK, "peer_connection_attempt_count_total", "Total number of outbound connection attempts made", "status");
        attemptedConnectionCounter = connectionAttemptCounter.labels("attempted");
        successfulConnectionCounter = connectionAttemptCounter.labels("successful");
        failedConnectionCounter = connectionAttemptCounter.labels("failed");

    }


    @Override
    protected SafeFuture<?> doStart() {
        LOG.info("Starting ConnectionService");
        activeNodesSearchingTask();
        return SafeFuture.COMPLETE;
    }

    private void activeNodesSearchingTask() {
        searchForActivePeers().alwaysRun(this::createNextSearchPeerTask).finish(this::logSearchError);
    }

    private SafeFuture<Void> searchForActivePeers() {
        if (!isRunning()) {
            LOG.info("Not running so not searching for active peers");
            return SafeFuture.COMPLETE;
        }
        LOG.info("Searching for active peers");
        return discv5Client.streamLiveNodes()
                .orTimeout(30, TimeUnit.SECONDS)
                .handle(
                        (peers, error) -> {
                            if (error == null) {
                                peers.stream()
                                        .filter(this::isPeerValid)
                                        .collect(Collectors.toSet())
                                        .forEach(this::connectToPeers);
                            } else {
                                LOG.info("Discovery failed", error);
                                //TODO  What to do ?
                            }
                            return null;
                        });
    }

    private void connectToPeers(final NodeRecord nodeRecord) {
        LOG.info("Attempting to connect to {}", nodeRecord.getNodeId());
        attemptedConnectionCounter.inc();
        network.connect(nodeRecord).finish(
                peer -> {
                    LOG.info("Successfully connected to node {}", nodeRecord.getNodeId());
                    successfulConnectionCounter.inc();

//                    peer.subscribeDisconnect(
//                            (reason, locallyInitiated) -> peerPools.forgetPeer(peer.getId()));
                },
                error -> {
                    LOG.trace(() -> "Failed to connect to node: " + nodeRecord.getNodeId());
                    failedConnectionCounter.inc();
//                    peerPools.forgetPeer(peerAddress.getId());
                });

    }


    private boolean isPeerValid(final NodeRecord NodeRecord) {
        //TODO What to validate?
        return true;
    }


    @Override
    protected SafeFuture<?> doStop() {
        //  network.unsubscribeConnect(peerConnectedSubscriptionId);
        cancelPeerSearchTask();
        return SafeFuture.COMPLETE;
    }

    private void cancelPeerSearchTask() {
        final Cancellable peerSearchTask = this.periodicPeerSearch;
        if (peerSearchTask != null) {
            peerSearchTask.cancel();
        }
    }


    private void createNextSearchPeerTask() {
        if (network.getPeerCount() == 0) {
            // Retry fast until we have at least one connection with peers
            LOG.trace("Retrying peer search, no connected peers yet");
            cancelPeerSearchTask();
            this.periodicPeerSearch = asyncRunner.runCancellableAfterDelay(this::activeNodesSearchingTask, WARMUP_DISCOVERY_INTERVAL, this::logSearchError);
        } else {
            // Long term task, run when we have peers connected
            LOG.trace("Establishing peer search task with long delay");
            cancelPeerSearchTask();
            this.periodicPeerSearch =
                    asyncRunner.runWithFixedDelay(
                            () -> searchForActivePeers().finish(this::logSearchError),
                            DISCOVERY_INTERVAL,
                            this::logSearchError);
        }
    }

    private void logSearchError(final Throwable throwable) {
        LOG.error("Error while searching for peers", throwable);
    }
}

/**
 *
 *                private volatile Cancellable bootnodeRefreshTask;
 *               .thenRun(
 *                         () ->{
 *                             this.bootnodeRefreshTask =
 *                                     asyncRunner.runWithFixedDelay(
 *                                             this::pingBootnodes,
 *                                             BOOTNODE_REFRESH_DELAY,
 *                                             error -> LOG.error("Failed to contact discovery bootnodes", error));
 *                         }
 *                 );
 *     }
 *
 *     private void pingBootnodes() {
 *         bootnodes.forEach(
 *                 bootnode ->
 *
 *                         SafeFuture.of(discoverySystem.ping(bootnode).whenComplete((a,b) -> System.out.println(a+ ""+ b)))
 *                                 .whenComplete((a,b) -> System.out.println(a))
 *                                 .finish(error -> LOG.info("Bootnode {} is unresponsive", bootnode)));
 *     }
 *
 *
 *     final Cancellable refreshTask = this.bootnodeRefreshTask;
 *         this.bootnodeRefreshTask = null;
 *         if (refreshTask != null) {
 *             refreshTask.cancel();
 *         }
 */