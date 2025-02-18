package samba;

import static samba.logging.StatusLogger.STATUS_LOG;
import static tech.pegasys.teku.infrastructure.time.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import samba.config.PortalRestApiConfig;
import samba.config.SambaConfiguration;
import samba.config.StartupLogConfig;
import samba.config.VersionProvider;
import samba.metrics.MetricsEndpoint;
import samba.node.Node;
import samba.services.MainServiceConfig;
import samba.services.PortalNodeMainController;
import samba.util.PortalDefaultExceptionHandler;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;
import tech.pegasys.teku.infrastructure.events.EventChannels;

public class PortalNode implements Node {

  private static final Logger LOG = LogManager.getLogger();

  private final Vertx vertx = Vertx.vertx();
  private final ExecutorService threadPool =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setDaemon(true).setNameFormat("events-%d").build());
  private final EventChannels eventChannels;

  // async actions
  private final AsyncRunnerFactory asyncRunnerFactory;

  private final MetricsEndpoint metricsEndpoint;

  private final PortalNodeMainController portalNodeMainController;

  private final OccurrenceCounter rejectedExecutionCounter = new OccurrenceCounter(120);
  private Optional<Cancellable> counterMaintainer = Optional.empty();

  // private final MetricsPublisherManager metricsPublisher;

  public PortalNode(final SambaConfiguration sambaConfiguration) {
    this.metricsEndpoint = new MetricsEndpoint(sambaConfiguration.getMetricsConfig(), vertx);
    this.eventChannels =
        new EventChannels(new PortalDefaultExceptionHandler(), metricsEndpoint.getMetricsSystem());
    this.asyncRunnerFactory =
        AsyncRunnerFactory.createDefault(
            new MetricTrackingExecutorFactory(
                metricsEndpoint.getMetricsSystem(), rejectedExecutionCounter));

    final PortalRestApiConfig portalRestApiConfig = sambaConfiguration.getPortalRestApiConfig();
    STATUS_LOG.onStartup("1.0 " + "Commit: " + VersionProvider.COMMIT_HASH);
    STATUS_LOG.startupConfigurations(
        StartupLogConfig.builder()
            .network("")
            .hardwareInfo(new SystemInfo().getHardware())
            .portalNodeRestApiEnabled(portalRestApiConfig.isRestApiDocsEnabled())
            .portalNodeRestApiInterface(portalRestApiConfig.getRestApiInterface())
            .portalNodeRestApiPort(portalRestApiConfig.getRestApiPort())
            .portalNodeRestApiAllowList(portalRestApiConfig.getRestApiHostAllowlist())
            .build());

    final MainServiceConfig mainServiceConfig =
        new MainServiceConfig(
            asyncRunnerFactory,
            SYSTEM_TIME_PROVIDER,
            eventChannels,
            metricsEndpoint.getMetricsSystem(),
            rejectedExecutionCounter::getTotalCount);
    this.portalNodeMainController =
        new PortalNodeMainController(mainServiceConfig, sambaConfiguration, vertx);

    // final String network =
    // tekuConfig.eth2NetworkConfiguration().getEth2Network().map(Eth2Network::configName).orElse("empty");
  }

  @Override
  public void start() {
    metricsEndpoint.start().join();
    this.portalNodeMainController.start().join();
    //      counterMaintainer =
    //              Optional.of(
    //                      serviceConfig
    //                              .createAsyncRunner("RejectedExecutionCounter", 1)
    //                              .runWithFixedDelay(
    //                                      this::pollRejectedExecutions,
    //                                      Duration.ofSeconds(5),
    //                                      (err) -> LOG.debug("rejected execution poll failed",
    // err)));
  }

  @Override
  public void stop() {
    this.eventChannels
        .stop()
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.warn("Failed to stop event channels cleanly", error))
        .join();
    threadPool.shutdownNow();
    counterMaintainer.ifPresent(Cancellable::cancel);
    asyncRunnerFactory.shutdown();

    // Stop services.
    this.portalNodeMainController
        .stop()
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.error("Failed to stop services", error))
        .orTimeout(5, TimeUnit.SECONDS)
        .handleException(error -> LOG.debug("Failed to stop metrics", error))
        .thenRun(vertx::close)
        .join();
  }
}
