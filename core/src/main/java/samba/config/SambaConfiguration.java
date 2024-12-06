package samba.config;

import java.util.List;

public class SambaConfiguration {
  public static final String DEFAULT_NETWORK_NAME = "mainnet";

  private final MetricsConfig metricsConfig;
  private final PortalRestApiConfig portalRestApiConfig;
  private final String networkName;

  private SambaConfiguration(
      final MetricsConfig metricsConfig,
      final PortalRestApiConfig portalRestApiConfig,
      final String networkName) {
    this.metricsConfig = metricsConfig;
    this.portalRestApiConfig = portalRestApiConfig;
    this.networkName = networkName;
  }

  public MetricsConfig metricsConfig() {
    return metricsConfig;
  }

  public PortalRestApiConfig portalRestApiConfig() {
    return portalRestApiConfig;
  }

  public String getNetworkName() {
    return networkName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final MetricsConfig.MetricsConfigBuilder metricsConfigBuilder = MetricsConfig.builder();
    private final PortalRestApiConfig.PortalRestApiConfigBuilder portalRestApiConfig =
        PortalRestApiConfig.builder();
    private String networkName;

    private Builder() {}

    public SambaConfiguration build() {
      initMissingDefaults();

      return new SambaConfiguration(
          metricsConfigBuilder.metricsEnabled(true).build(),
          portalRestApiConfig.restApiCorsAllowedOrigins(List.of("127.0.0.1", "localhost")).build(),
          networkName);
    }

    private void initMissingDefaults() {
      if (networkName == null) {
        networkName = DEFAULT_NETWORK_NAME;
      }
    }

    public Builder setNetwork(final String networkName) {
      this.networkName = networkName;
      return this;
    }
  }
}
