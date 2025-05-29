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

package samba.metrics;

import samba.config.MetricsConfig;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.hyperledger.besu.metrics.MetricsService;
import org.hyperledger.besu.metrics.ObservableMetricsSystem;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.metrics.prometheus.MetricsConfiguration;
import org.hyperledger.besu.metrics.prometheus.PrometheusMetricsSystem;
import org.hyperledger.besu.plugin.services.MetricsSystem;

public class MetricsEndpoint {

  private final Optional<MetricsService> metricsService;
  private final ObservableMetricsSystem metricsSystem;

  public MetricsEndpoint(final MetricsConfig config) {
    final MetricsConfiguration metricsConfig = createMetricsConfiguration(config);
    if (metricsConfig.isEnabled()) {
      metricsSystem = new PrometheusMetricsSystem(config.getMetricsCategories(), true);
      // TODO check if we need the metricSystem.init()
      metricsService = MetricsService.create(metricsConfig, metricsSystem);
    } else {
      metricsSystem = new NoOpMetricsSystem();
      metricsService = MetricsService.create(metricsConfig, metricsSystem);
    }
  }

  public CompletableFuture<?> start() {
    return metricsService
        .map(MetricsService::start)
        .orElse(CompletableFuture.completedFuture(null));
  }

  public CompletableFuture<?> stop() {
    return metricsService.map(MetricsService::stop).orElse(CompletableFuture.completedFuture(null));
  }

  public MetricsSystem getMetricsSystem() {
    return metricsSystem;
  }

  private MetricsConfiguration createMetricsConfiguration(final MetricsConfig config) {
    return MetricsConfiguration.builder()
        .enabled(config.isMetricsEnabled())
        .port(config.getMetricsPort())
        .host(config.getMetricsInterface())
        .metricCategories(config.getMetricsCategories())
        .hostsAllowlist(config.getMetricsHostAllowlist())
        .idleTimeout(config.getIdleTimeoutSeconds())
        .build();
  }
}
