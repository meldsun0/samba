package samba.metrics;

import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

import java.util.Optional;
import java.util.Set;

public enum SambaMetricCategory implements MetricCategory {
    DISCOVERY("discovery"),
    EVENTBUS("eventbus"),
    EXECUTOR("executor"),
    LIBP2P("libp2p"),
    NETWORK("network"),
    STORAGE("storage");


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
        return Set.of(DISCOVERY, EVENTBUS, EXECUTOR, LIBP2P, NETWORK);
    }
}
