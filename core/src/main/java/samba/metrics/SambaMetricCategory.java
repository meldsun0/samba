package samba.metrics;

import java.util.Optional;
import java.util.Set;

import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

public enum SambaMetricCategory implements MetricCategory {
  DISCOVERY("samba_discovery"),
  NETWORK("samba_network"),
  STORAGE("samba_storage"),
  HISTORY("samba_history");

  private final String name;

  SambaMetricCategory(final String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getApplicationPrefix() {
    return Optional.empty();
  }

  public static Set<SambaMetricCategory> defaultCategories() {
    return Set.of(DISCOVERY, NETWORK, STORAGE, HISTORY);
  }
}
