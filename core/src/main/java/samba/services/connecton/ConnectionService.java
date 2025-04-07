package samba.services.connecton;

import samba.network.Network;
import samba.services.discovery.Discv5Client;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class ConnectionService extends Service {

  private static final Logger LOG = LogManager.getLogger();
  protected static final Duration WARMUP_DISCOVERY_INTERVAL = Duration.ofSeconds(1);
  protected static final Duration DISCOVERY_INTERVAL = Duration.ofSeconds(30);

  private final Discv5Client discv5Client;
  private final Network network;

  //  private final Counter attemptedConnectionCounter;
  //  private final Counter successfulConnectionCounter;
  //  private final Counter failedConnectionCounter;
  private final AsyncRunner asyncRunner;
  // asyncRunnerFactory.shutdown();
  private volatile Cancellable periodicPeerSearch;

  public ConnectionService(
      final AsyncRunner asyncRunner, final Discv5Client discv5Client, final Network network) {
    this.asyncRunner = asyncRunner;
    this.network = network;
    this.discv5Client = discv5Client;

    //    final LabelledMetric<Counter> connectionAttemptCounter =
    //        metricsSystem.createLabelledCounter(
    //            SambaMetricCategory.NETWORK,
    //            "peer_connection_attempt_count_total",
    //            "Total number of outbound connection attempts made",
    //            "status");
    //    attemptedConnectionCounter = connectionAttemptCounter.labels("attempted");
    //    successfulConnectionCounter = connectionAttemptCounter.labels("successful");
    //    failedConnectionCounter = connectionAttemptCounter.labels("failed");
  }

  @Override
  protected SafeFuture<?> doStart() {
    activeNodesSearchingTask();
    return SafeFuture.COMPLETE;
  }

  private void activeNodesSearchingTask() {
    searchForActivePeers().alwaysRun(this::createNextSearchPeerTask).finish(this::logSearchError);
  }

  private SafeFuture<Void> searchForActivePeers() {
    if (!isRunning()) {
      LOG.trace("Not running so not searching for active peers");
      return SafeFuture.COMPLETE;
    }
    LOG.info(
        "{} active peers. Checking again in {} seconds",
        network.getNumberOfConnectedPeers(),
        DISCOVERY_INTERVAL.getSeconds());

    return discv5Client
        .streamLiveNodes()
        .orTimeout(30, TimeUnit.SECONDS)
        .handle(
            (peers, error) -> {
              if (error == null) {
                peers.stream()
                    .filter(this::isPeerValid)
                    .collect(Collectors.toSet())
                    .forEach(this::connectToPeers);
              } else {
                LOG.trace("Discovery failed", error);
                // TODO  What to do ?
              }
              return null;
            });
  }

  private void connectToPeers(final NodeRecord nodeRecord) {
    LOG.trace("Attempting to connect to {}", nodeRecord.getNodeId());

    //   attemptedConnectionCounter.inc();
    network
        .ping(nodeRecord)
        .finish(
            peer -> {
              LOG.trace("Successfully connected to node {}", nodeRecord.getNodeId());
              //           successfulConnectionCounter.inc();
              //                    peer.subscribeDisconnect((reason, locallyInitiated) ->
              // peerPools.forgetPeer(peer.getId()));
            },
            error -> {
              LOG.trace(() -> "Failed to connect to node: " + nodeRecord.getNodeId());
              //         failedConnectionCounter.inc();
              //                    peerPools.forgetPeer(peerAddress.getId());
            });
  }

  private boolean isPeerValid(final NodeRecord NodeRecord) {
    // TODO What to validate?
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
    if (network.getNumberOfConnectedPeers() == 0) {
      LOG.trace("Retrying peer search, no connected peers yet");
      cancelPeerSearchTask();
      this.periodicPeerSearch =
          asyncRunner.runCancellableAfterDelay(
              this::activeNodesSearchingTask, WARMUP_DISCOVERY_INTERVAL, this::logSearchError);
    } else {
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
