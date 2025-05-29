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

import samba.metrics.SambaMetricCategory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

public class MetricsConfig {

  public static final ImmutableSet<MetricCategory> DEFAULT_METRICS_CATEGORIES =
      ImmutableSet.<MetricCategory>builder()
          .addAll(SambaMetricCategory.defaultCategories())
          .build();
  public static final int DEFAULT_METRICS_PORT = 8008;
  public static final String DEFAULT_METRICS_INTERFACE = "0.0.0.0";
  public static final List<String> DEFAULT_METRICS_HOST_ALLOWLIST =
      Arrays.asList("127.0.0.1", "localhost", "samba", "prometheus");
  public static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 60;
  public static final int DEFAULT_METRICS_PUBLICATION_INTERVAL = 60;

  private final boolean metricsEnabled;
  private final int metricsPort;
  private final String metricsInterface;
  private final Set<MetricCategory> metricsCategories;
  private final List<String> metricsHostAllowlist;
  private final Optional<URL> metricsEndpoint;
  private final int publicationInterval;
  private final int idleTimeoutSeconds;

  private MetricsConfig(
      final boolean metricsEnabled,
      final int metricsPort,
      final String metricsInterface,
      final Set<MetricCategory> metricsCategories,
      final List<String> metricsHostAllowlist,
      final URL metricsEndpoint,
      final int publicationInterval,
      final int idleTimeoutSeconds) {
    this.metricsEnabled = metricsEnabled;
    this.metricsPort = metricsPort;
    this.metricsInterface = metricsInterface;
    this.metricsCategories = metricsCategories;
    this.metricsHostAllowlist = metricsHostAllowlist;
    this.metricsEndpoint = Optional.ofNullable(metricsEndpoint);
    this.publicationInterval = publicationInterval;
    this.idleTimeoutSeconds = idleTimeoutSeconds;
  }

  public static MetricsConfigBuilder builder() {
    return new MetricsConfigBuilder();
  }

  public boolean isMetricsEnabled() {
    return metricsEnabled;
  }

  public int getMetricsPort() {
    return metricsPort;
  }

  public String getMetricsInterface() {
    return metricsInterface;
  }

  public Set<MetricCategory> getMetricsCategories() {
    return metricsCategories;
  }

  public List<String> getMetricsHostAllowlist() {
    return metricsHostAllowlist;
  }

  public Optional<URL> getMetricsEndpoint() {
    return metricsEndpoint;
  }

  public int getPublicationInterval() {
    return publicationInterval;
  }

  public int getIdleTimeoutSeconds() {
    return idleTimeoutSeconds;
  }

  public Collection<String> getMetricsConfigSummaryLog() {
    List<String> summary = new ArrayList<>();
    summary.add("Metrics Server Summary:");
    if (!this.metricsEnabled) {
      summary.add("Enable: false");
    } else {
      summary.add(
          String.format(
              "Enabled: true, Listen Address: %s, Port: %s", metricsInterface, metricsPort));
      summary.add(
          String.format(
              "Allow: %s, Categories: %s",
              String.join(",", this.metricsHostAllowlist),
              String.join(
                  ",", this.metricsCategories.stream().map(MetricCategory::getName).toList())));
    }
    return summary;
  }

  public static final class MetricsConfigBuilder {

    private boolean metricsEnabled = true;
    private final int metricsPort = DEFAULT_METRICS_PORT;
    private String metricsInterface = DEFAULT_METRICS_INTERFACE;
    private Set<MetricCategory> metricsCategories = DEFAULT_METRICS_CATEGORIES;
    private List<String> metricsHostAllowlist = DEFAULT_METRICS_HOST_ALLOWLIST;
    private URL metricsPublishEndpoint = null;
    private final int metricsPublishInterval = DEFAULT_METRICS_PUBLICATION_INTERVAL;
    private final int idleTimeoutSeconds = DEFAULT_IDLE_TIMEOUT_SECONDS;

    private MetricsConfigBuilder() {}

    public MetricsConfigBuilder metricsEnabled(final boolean metricsEnabled) {
      this.metricsEnabled = metricsEnabled;
      return this;
    }

    public MetricsConfigBuilder metricsInterface(final String metricsInterface) {
      this.metricsInterface = metricsInterface;
      return this;
    }

    public MetricsConfigBuilder metricsCategories(final Set<MetricCategory> metricsCategories) {
      this.metricsCategories = metricsCategories;
      return this;
    }

    public MetricsConfigBuilder metricsHostAllowlist(final List<String> metricsHostAllowlist) {
      this.metricsHostAllowlist = metricsHostAllowlist;
      return this;
    }

    public MetricsConfigBuilder metricsPublishEndpoint(final URL metricsPublishEndpoint) {
      this.metricsPublishEndpoint = metricsPublishEndpoint;
      return this;
    }

    public MetricsConfig build() {
      return new MetricsConfig(
          metricsEnabled,
          metricsPort,
          metricsInterface,
          metricsCategories,
          metricsHostAllowlist,
          metricsPublishEndpoint,
          metricsPublishInterval,
          idleTimeoutSeconds);
    }
  }
}
