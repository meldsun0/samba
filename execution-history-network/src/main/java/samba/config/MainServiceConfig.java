package samba.config;

import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;

import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;

public class MainServiceConfig {

  private final AsyncRunnerFactory asyncRunnerFactory;

  private static final int DEFAULT_ASYNC_MAX_THREADS = 10;
  public static final int DEFAULT_ASYNC_MAX_QUEUE = DEFAULT_MAX_QUEUE_SIZE;

  public MainServiceConfig(final AsyncRunnerFactory asyncRunnerFactory) {
    this.asyncRunnerFactory = asyncRunnerFactory;
  }

  public AsyncRunner createAsyncRunner(final String name) {
    return asyncRunnerFactory.create(name, DEFAULT_ASYNC_MAX_THREADS, DEFAULT_ASYNC_MAX_QUEUE);
  }
}
