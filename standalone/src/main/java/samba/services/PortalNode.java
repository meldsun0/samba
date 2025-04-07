package samba.services;

import static samba.logging.StatusLogger.STATUS_LOG;
import static tech.pegasys.teku.infrastructure.time.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import samba.Samba;
import samba.SambaInitializer;
import samba.config.MainServiceConfig;
import samba.config.MetricsConfig;
import samba.config.PortalRestApiConfig;
import samba.config.SambaConfig;
import samba.config.StartupLogConfig;
import samba.exceptions.PortalDefaultExceptionHandler;
import samba.jsonrpc.config.JsonRpcConfiguration;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.health.HealthService;
import samba.jsonrpc.health.LivenessCheck;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.metrics.MetricsEndpoint;
import samba.services.api.PortalAPI;
import samba.services.api.PortalRestAPI;
import samba.services.jsonrpc.JsonRpcService;
import samba.services.jsonrpc.methods.ClientVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.DefaultAsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;

public class PortalNode {

  private static final Logger LOG = LogManager.getLogger();

  private final Vertx vertx = Vertx.vertx();
  private final ExecutorService threadPool =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setDaemon(true).setNameFormat("events-%d").build());
  private final EventChannels eventChannels;
  private final MetricsEndpoint metricsEndpoint;

  private final MainServiceConfig mainServiceConfig;
  private final PortalRestApiConfig portalRestApiConfig;
  private final JsonRpcConfiguration jsonRpcConfiguration;

  private volatile SambaConfig sambaConfig;

  protected volatile Optional<PortalRestAPI> portalRestAPI = Optional.empty();
  protected volatile Optional<JsonRpcService> jsonRpcService = Optional.empty();
  protected volatile Optional<Samba> samba = Optional.empty();

  public PortalNode(String[] args) {
    this.metricsEndpoint = new MetricsEndpoint(MetricsConfig.builder().build(), vertx);
    this.eventChannels =
        new EventChannels(new PortalDefaultExceptionHandler(), metricsEndpoint.getMetricsSystem());
    this.portalRestApiConfig = PortalRestApiConfig.builder().build();
    this.jsonRpcConfiguration = JsonRpcConfiguration.createDefault();
    this.mainServiceConfig =
        new MainServiceConfig(
            SYSTEM_TIME_PROVIDER, eventChannels, metricsEndpoint.getMetricsSystem());

    STATUS_LOG.startupConfigurations(
        StartupLogConfig.builder()
            .portalNodeRestApiEnabled(portalRestApiConfig.isRestApiDocsEnabled())
            .portalNodeRestApiInterface(portalRestApiConfig.getRestApiInterface())
            .portalNodeRestApiPort(portalRestApiConfig.getRestApiPort())
            .portalNodeRestApiAllowList(portalRestApiConfig.getRestApiHostAllowlist())
            .build());

    initHistoryNetwork(args);
    initRestAPI();
    initJsonRPCService();
  }

  public void start() {
    LOG.debug("Starting {}", this.getClass().getSimpleName());
    metricsEndpoint.start().join();

    //        SafeFuture.allOfFailFast( __ -> this.metricsEndpoint.start())
    //                .thenCompose(
    //                        __ ->
    //
    // jsonRpcService.map(JsonRpcService::start).orElse(SafeFuture.completedFuture(null)))
    //                .thenCompose(
    //                        __ ->
    // portalRestAPI.map(PortalRestAPI::start).orElse(SafeFuture.completedFuture(null)))
    //                .start()
    //                .join();

    SafeFuture.allOfFailFast(
            this.jsonRpcService.map(JsonRpcService::start).orElse(SafeFuture.completedFuture(null)))
        .thenCompose(
            __ -> portalRestAPI.map(PortalRestAPI::start).orElse(SafeFuture.completedFuture(null)))
        .join();
  }

  public void stop() {
    LOG.debug("Stopping {}", this.getClass().getSimpleName());
    this.eventChannels
        .stop()
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.warn("Failed to stop event channels cleanly", error))
        .join();
    threadPool.shutdownNow();
    this.samba.ifPresent(Samba::stop);
    SafeFuture.allOf(
            //  this.samba.map(Samba::stop).orElse(SafeFuture.completedFuture(null)),
            portalRestAPI.map(PortalRestAPI::stop).orElse(SafeFuture.completedFuture(null)))
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.error("Failed to stop services", error))
        .orTimeout(5, TimeUnit.SECONDS)
        .handleException(error -> LOG.debug("Failed to stop metrics", error))
        .thenRun(vertx::close)
        .join();
  }

  private void initHistoryNetwork(String[] args) {
    this.samba = SambaInitializer.start(args);
  }

  public void initRestAPI() {
    portalRestAPI =
        Optional.of(
            new PortalAPI(
                this.portalRestApiConfig,
                eventChannels,
                this.createAsyncRunner(),
                this.mainServiceConfig.timeProvider()));
  }

  private void initJsonRPCService() {
    final Map<String, JsonRpcMethod> methods = new HashMap<>();

    methods.put(RpcMethod.CLIENT_VERSION.getMethodName(), new ClientVersion("1"));

    jsonRpcService =
        Optional.of(
            new JsonRpcService(
                this.vertx,
                jsonRpcConfiguration,
                this.mainServiceConfig.metricsSystem(),
                methods,
                new HealthService(new LivenessCheck())));
  }

  private AsyncRunner createAsyncRunner() {
    DefaultAsyncRunnerFactory asyncRunnerFactory =
        AsyncRunnerFactory.createDefault(
            new MetricTrackingExecutorFactory(new NoOpMetricsSystem(), new OccurrenceCounter(120)));
    return asyncRunnerFactory.create("rest-api, 10", 10, AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE);
  }
}
