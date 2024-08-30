/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package samba.config;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;


import java.util.function.IntSupplier;

public class ServiceConfig {

  private final AsyncRunnerFactory asyncRunnerFactory;
  private final TimeProvider timeProvider;
  private final EventChannels eventChannels;
  private final MetricsSystem metricsSystem;


  private final IntSupplier rejectedExecutionsSupplier;
  private final int executorThreads = Math.max(5, Math.min(Runtime.getRuntime().availableProcessors(), 12));

  public ServiceConfig(
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

  public TimeProvider getTimeProvider() {
    return timeProvider;
  }

  public EventChannels getEventChannels() {
    return eventChannels;
  }

  public MetricsSystem getMetricsSystem() {
    return metricsSystem;
  }

  public IntSupplier getRejectedExecutionsSupplier() {
    return rejectedExecutionsSupplier;
  }

  public AsyncRunner createAsyncRunner(final String name) {
    return createAsyncRunner(name, calculateMaxThreads());
  }

  public AsyncRunner createAsyncRunnerWithMaxQueueSize(final String name, final int maxQueueSize) {
    return createAsyncRunner(name, calculateMaxThreads(), maxQueueSize);
  }

  public AsyncRunner createAsyncRunner(final String name, final int maxThreads) {
    return asyncRunnerFactory.create(name, maxThreads);
  }

  public AsyncRunner createAsyncRunner(
      final String name, final int maxThreads, final int maxQueueSize) {
    return asyncRunnerFactory.create(name, maxThreads, maxQueueSize);
  }

  public AsyncRunner createAsyncRunner(
      final String name, final int maxThreads, final int maxQueueSize, final int threadPriority) {
    return asyncRunnerFactory.create(name, maxThreads, maxQueueSize, threadPriority);
  }

  public AsyncRunnerFactory getAsyncRunnerFactory() {
    return asyncRunnerFactory;
  }

  private int calculateMaxThreads() {
    // We use a bunch of blocking calls so need to ensure the thread pool is reasonably large
    // as many threads may be blocked.
    return Math.max(Runtime.getRuntime().availableProcessors(), executorThreads);
  }
}
