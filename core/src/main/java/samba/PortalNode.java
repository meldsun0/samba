package samba;

import static samba.logging.StatusLogger.STATUS_LOG;
import static tech.pegasys.teku.infrastructure.time.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import samba.async.SambaAsyncRunnerFactory;
import samba.async.SambaTrackingExecutorFactory;
import samba.config.RestServerConfig;
import samba.config.SambaConfiguration;
import samba.config.StartupLogConfig;
import samba.config.VersionProvider;
import samba.metrics.MetricsEndpoint;
import samba.services.HistoryNetworkMainService;
import samba.services.HistoryNetworkMainServiceConfig;
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
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;
import tech.pegasys.teku.infrastructure.events.EventChannels;

public final class PortalNode implements AutoCloseable {

  private static final Logger LOG = LogManager.getLogger();
  // TODO one jsonrpc server for all sub-networks
  private final Vertx vertx = Vertx.vertx();
  private final ExecutorService threadPool =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setDaemon(true).setNameFormat("events-%d").build());
  private final EventChannels eventChannels;

  // async actions
  private final AsyncRunnerFactory asyncRunnerFactory;
  private final MetricsEndpoint metricsEndpoint;
  private final HistoryNetworkMainService historyNetworkMainService;

  private final OccurrenceCounter rejectedExecutionCounter = new OccurrenceCounter(120);
  private Optional<Cancellable> counterMaintainer = Optional.empty();
  private SambaSDK sambaSDK;

  // private final MetricsPublisherManager metricsPublisher;

  public PortalNode(final SambaConfiguration sambaConfiguration) {
    this.metricsEndpoint = new MetricsEndpoint(sambaConfiguration.getMetricsConfig());
    this.eventChannels = new EventChannels(new PortalDefaultExceptionHandler(), metricsEndpoint.getMetricsSystem());
    this.asyncRunnerFactory = new SambaAsyncRunnerFactory(new SambaTrackingExecutorFactory(rejectedExecutionCounter, metricsEndpoint.getMetricsSystem()));

    final RestServerConfig restServerConfig = sambaConfiguration.getRestServerConfig();
    STATUS_LOG.onStartup(
        "1.0 " + (VersionProvider.COMMIT_HASH.map(s -> "Commit: " + s).orElse("")));
    STATUS_LOG.startupConfigurations(
        StartupLogConfig.builder()
            .network("")
            .hardwareInfo(new SystemInfo().getHardware())
            .portalNodeRestApiEnabled(restServerConfig.isRestApiDocsEnabled())
            .portalNodeRestApiInterface(restServerConfig.getRestApiInterface())
            .portalNodeRestApiPort(restServerConfig.getRestApiPort())
            .portalNodeRestApiAllowList(restServerConfig.getRestApiHostAllowlist())
            .build());

    final HistoryNetworkMainServiceConfig historyNetworkMainServiceConfig =
        new HistoryNetworkMainServiceConfig(
            asyncRunnerFactory,
            SYSTEM_TIME_PROVIDER,
            eventChannels,
            metricsEndpoint.getMetricsSystem(),
            rejectedExecutionCounter::getTotalCount);
    this.historyNetworkMainService =
        new HistoryNetworkMainService(historyNetworkMainServiceConfig, sambaConfiguration, vertx);
  }

  public void start() {
    metricsEndpoint.start().join();
    this.historyNetworkMainService
        .start()
        .whenComplete(
            (__, error) -> {
              if (error != null) {
                LOG.info("Error when Starting Samba", error);
              }
              try {
                initSambaSDK();
              } catch (Exception e) {
                LOG.error("Error when building Samba SDK", e);
              }
            })
        .join();

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
    this.historyNetworkMainService
        .stop()
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.error("Failed to stop services", error))
        .orTimeout(5, TimeUnit.SECONDS)
        .handleException(error -> LOG.debug("Failed to stop metrics", error))
        .thenRun(vertx::close)
        .join();
  }

  @Override
  public void close() throws Exception {
    stop();
  }

  private void initSambaSDK() {
    // TODO read config to decide what SDK to include
    this.sambaSDK =
        SambaSDK.builder()
            .withHistoryAPI(this.historyNetworkMainService.getSDK())
            .withDiscv5API(this.historyNetworkMainService.getDiscv5API())
            .build();
  }

  SambaSDK getSambaSDK() {
    return this.sambaSDK;
  }

  /*
  * samba.historyAPI().ifPresent(history -> {
            System.out.println("Using HistoryAPI...");
            history.getEnr("abc123").ifPresent(enr -> System.out.println("ENR: " + enr));
            history.getLocalContent(Bytes.fromHexString("0xdeadbeef"))
                   .ifPresent(content -> System.out.println("Local content: " + content));
        });
  *
  * */
}
