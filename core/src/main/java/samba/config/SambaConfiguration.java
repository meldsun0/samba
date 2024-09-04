package samba.config;

import java.util.List;
import java.util.function.Consumer;

public class SambaConfiguration {

    private final MetricsConfig metricsConfig;
    private final PortalRestApiConfig portalRestApiConfig;

    private SambaConfiguration(final MetricsConfig metricsConfig, final PortalRestApiConfig portalRestApiConfig) {
        this.metricsConfig = metricsConfig;
        this.portalRestApiConfig = portalRestApiConfig;
    }

    public MetricsConfig metricsConfig() {
        return metricsConfig;
    }

    public PortalRestApiConfig portalRestApiConfig() {
        return portalRestApiConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MetricsConfig.MetricsConfigBuilder metricsConfigBuilder = MetricsConfig.builder();
        private final PortalRestApiConfig.PortalRestApiConfigBuilder portalRestApiConfig =  PortalRestApiConfig.builder();
        private Builder() {}

        public SambaConfiguration build() {

            return new SambaConfiguration(
                    metricsConfigBuilder.metricsEnabled(true).build(),
                    portalRestApiConfig.restApiCorsAllowedOrigins( List.of("127.0.0.1", "localhost")).build());
        }

    }

}
