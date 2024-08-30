package samba.config;

import java.util.function.Consumer;

public class SambaConfiguration {

    private final MetricsConfig metricsConfig;


    private SambaConfiguration(final MetricsConfig metricsConfig) {
        this.metricsConfig = metricsConfig;
    }

    public MetricsConfig metricsConfig() {
        return metricsConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MetricsConfig.MetricsConfigBuilder metricsConfigBuilder = MetricsConfig.builder();

        private Builder() {}

        public SambaConfiguration build() {

            return new SambaConfiguration(metricsConfigBuilder.build());
        }

    }

}
