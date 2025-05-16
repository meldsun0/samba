package samba.async;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.ExceptionThrowingFutureSupplier;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class SambaScheduledExecutorAsyncRunner implements AsyncRunner {

  private static final Logger LOG =
      LoggerFactory.getLogger(SambaScheduledExecutorAsyncRunner.class);
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final ScheduledExecutorService scheduler;
  private final ExecutorService workerPool;

  SambaScheduledExecutorAsyncRunner(
      ScheduledExecutorService scheduler, ExecutorService workerPool) {
    this.scheduler = scheduler;
    this.workerPool = workerPool;
  }

  public static AsyncRunner create(
      String name,
      int maxThreads,
      int maxQueueSize,
      int threadPriority,
      SambaTrackingExecutorFactory executorFactory) {
    ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat(name + "-async-scheduler-%d")
                .setDaemon(false)
                .build());
    ExecutorService workerPool =
        executorFactory.newCachedThreadPool(
            name,
            maxThreads,
            maxQueueSize,
            (new ThreadFactoryBuilder())
                .setNameFormat(name + "-async-%d")
                .setDaemon(false)
                .setPriority(threadPriority)
                .build());
    return new SambaScheduledExecutorAsyncRunner(scheduler, workerPool);
  }

  public <U> SafeFuture<U> runAsync(ExceptionThrowingFutureSupplier<U> action) {
    if (this.shutdown.get()) {
      LOG.debug("Ignoring async task because shutdown is in progress");
      return new SafeFuture();
    } else {
      SafeFuture<U> result = new SafeFuture();

      try {
        this.workerPool.execute(this.createRunnableForAction(action, result));
      } catch (Throwable var4) {
        Throwable t = var4;
        this.handleExecutorError(result, t);
      }

      return result;
    }
  }

  public <U> SafeFuture<U> runAfterDelay(
      ExceptionThrowingFutureSupplier<U> action, Duration delay) {
    if (this.shutdown.get()) {
      LOG.debug("Ignoring async task because shutdown is in progress");
      return new SafeFuture();
    } else {
      SafeFuture<U> result = new SafeFuture();

      try {
        this.scheduler.schedule(
            () -> {
              try {
                this.workerPool.execute(this.createRunnableForAction(action, result));
              } catch (Throwable var4) {
                Throwable t = var4;
                this.handleExecutorError(result, t);
              }
            },
            delay.toMillis(),
            TimeUnit.MILLISECONDS);
      } catch (Throwable var5) {
        Throwable t = var5;
        this.handleExecutorError(result, t);
      }

      return result;
    }
  }

  public void shutdown() {
    this.shutdown.set(true);
    this.scheduler.shutdownNow();
    this.workerPool.shutdownNow();
  }

  private <U> void handleExecutorError(SafeFuture<U> result, Throwable t) {
    if (t instanceof RejectedExecutionException && this.shutdown.get()) {
      LOG.trace("Ignoring RejectedExecutionException because shutdown is in progress", t);
    } else {
      result.completeExceptionally(t);
    }
  }

  private <U> Runnable createRunnableForAction(
      ExceptionThrowingFutureSupplier<U> action, SafeFuture<U> result) {
    return () -> {
      SafeFuture.of(action).propagateTo(result);
    };
  }
}
