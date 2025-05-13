package samba.async;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;

public class SambaAsyncRunnerFactory implements AsyncRunnerFactory {

  private final Collection<AsyncRunner> asyncRunners = new CopyOnWriteArrayList();
  private final SambaTrackingExecutorFactory sambaTrackingExecutorFactory;

  public SambaAsyncRunnerFactory(SambaTrackingExecutorFactory sambaTrackingExecutorFactory) {
    this.sambaTrackingExecutorFactory = sambaTrackingExecutorFactory;
  }

  @Override
  public AsyncRunner create(String name, int maxThreads, int maxQueueSize, int threadPriority) {
    this.validateAsyncRunnerName(name);
    AsyncRunner asyncRunner =
        SambaScheduledExecutorAsyncRunner.create(
            name, maxThreads, maxQueueSize, threadPriority, this.sambaTrackingExecutorFactory);
    this.asyncRunners.add(asyncRunner);
    return asyncRunner;
  }

  @Override
  public void shutdown() {
    this.asyncRunners.forEach(AsyncRunner::shutdown);
  }
}
