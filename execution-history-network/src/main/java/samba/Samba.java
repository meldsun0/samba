package samba;

import static samba.logging.StatusLogger.STATUS_LOG;

import samba.config.MainServiceConfig;
import samba.config.SambaConfig;
import samba.config.StartupLogConfig;
import samba.config.VersionProvider;
import samba.services.SambaMainService;

import java.util.concurrent.TimeUnit;

import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;

public final class Samba implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(Samba.class);

  private final AsyncRunnerFactory asyncRunnerFactory;
  private final SambaMainService sambaMainService;

  public Samba(final SambaConfig sambaConfig) {
    STATUS_LOG.onStartup(
        "1.0 " + (VersionProvider.COMMIT_HASH.map(s -> "Commit: " + s).orElse("")));
    STATUS_LOG.startupConfigurations(
        StartupLogConfig.builder()
            .network("")
            .hardwareInfo(new SystemInfo().getHardware())
            .build());
    this.asyncRunnerFactory = createDefaultAsyncRunnerFactory();
    final MainServiceConfig mainServiceConfig = new MainServiceConfig(this.asyncRunnerFactory);
    this.sambaMainService = new SambaMainService(sambaConfig, mainServiceConfig);
  }

  public void start() {
    this.sambaMainService.start().join();
  }

  public void stop() {
    asyncRunnerFactory.shutdown();
    this.sambaMainService
        .stop()
        .orTimeout(30, TimeUnit.SECONDS)
        .handleException(error -> LOG.error("Failed to stop services", error))
        .orTimeout(5, TimeUnit.SECONDS)
        .handleException(error -> LOG.debug("Failed to stop metrics", error))
        .join();
  }

  private AsyncRunnerFactory createDefaultAsyncRunnerFactory() {
    return AsyncRunnerFactory.createDefault(
        new MetricTrackingExecutorFactory(new NoOpMetricsSystem(), new OccurrenceCounter(120)));
  }

  @Override
  public void close() {
    this.stop();
  }
}
