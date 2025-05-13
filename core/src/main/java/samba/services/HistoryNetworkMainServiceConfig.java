package samba.services;

import java.util.function.IntSupplier;

import lombok.Getter;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;

public class HistoryNetworkMainServiceConfig {

  @Getter private final AsyncRunnerFactory asyncRunnerFactory;
  @Getter private final TimeProvider timeProvider;
  @Getter private final EventChannels eventChannels;
  @Getter private final MetricsSystem metricsSystem;

  @Getter private final IntSupplier rejectedExecutionsSupplier;
  private final int executorThreads =
      Math.max(5, Math.min(Runtime.getRuntime().availableProcessors(), 12));

  public HistoryNetworkMainServiceConfig(
      final AsyncRunnerFactory asyncRunnerFactory,
      final TimeProvider timeProvider,
      final EventChannels eventChannels,
      final MetricsSystem metricsSystem,
      final IntSupplier rejectedExecutionsSupplier) {
    this.asyncRunnerFactory = asyncRunnerFactory;
    this.timeProvider = timeProvider;
    this.eventChannels = eventChannels;
    this.metricsSystem = metricsSystem;

    this.rejectedExecutionsSupplier = rejectedExecutionsSupplier;
  }

  public AsyncRunner createAsyncRunner(
      final String name, final int maxThreads, final int maxQueueSize) {
    return asyncRunnerFactory.create(name, maxThreads, maxQueueSize);
  }
}
