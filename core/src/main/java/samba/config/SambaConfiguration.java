package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import samba.jsonrpc.config.JsonRpcConfiguration;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.util.Functions;

public class SambaConfiguration {
  public static final String DEFAULT_NETWORK_NAME = "mainnet";

  private final MetricsConfig metricsConfig;
  private final PortalRestApiConfig portalRestApiConfig;
  private final DiscoveryConfig discoveryConfig;
  private final JsonRpcConfiguration jsonRpcConfigurationn;

  private final SECP256K1.SecretKey secretKey;

  private SambaConfiguration(
      final MetricsConfig metricsConfig,
      final PortalRestApiConfig portalRestApiConfig,
      final DiscoveryConfig discoveryConfig,
      final JsonRpcConfiguration jsonRpcConfigurationn,
      final SECP256K1.SecretKey secretKey) {
    this.metricsConfig = metricsConfig;
    this.portalRestApiConfig = portalRestApiConfig;
    this.jsonRpcConfigurationn = jsonRpcConfigurationn;
    this.discoveryConfig = discoveryConfig;
    this.secretKey = secretKey;
  }

  public static Builder builder() {
    return new Builder();
  }

  public MetricsConfig getMetricsConfig() {
    return metricsConfig;
  }

  public PortalRestApiConfig getPortalRestApiConfig() {
    return portalRestApiConfig;
  }

  public DiscoveryConfig getDiscoveryConfig() {
    return discoveryConfig;
  }

  public SECP256K1.SecretKey getSecreteKey() {
    return secretKey;
  }

  public JsonRpcConfiguration getJsonRpcConfigurationn() {
    return jsonRpcConfigurationn;
  }

  public static class Builder {
    private final MetricsConfig.MetricsConfigBuilder metricsConfigBuilder = MetricsConfig.builder();
    private final PortalRestApiConfig.PortalRestApiConfigBuilder portalRestApiConfigBuilder =
        PortalRestApiConfig.builder();
    private final DiscoveryConfig.Builder discoveryConfigBuilder = DiscoveryConfig.builder();
    private final JsonRpcConfiguration jsonRpcConfiguration =
        JsonRpcConfiguration
            .createDefault(); // TODO change this once JsonConfiguration is turned into a builder

    private Optional<SECP256K1.SecretKey> secretKey = Optional.empty();

    private String networkName;

    private Builder() {}

    public SambaConfiguration build() {
      initMissingDefaults();

      return new SambaConfiguration(
          metricsConfigBuilder.build(),
          portalRestApiConfigBuilder.build(),
          discoveryConfigBuilder.build(),
          jsonRpcConfiguration, // TODO change this once JsonConfiguration is turned into a builder
          secretKey.get());
    }

    private void initMissingDefaults() {
      if (networkName == null) {
        networkName = DEFAULT_NETWORK_NAME;
      }
      secretKey = secretKey.or(this::createRandomSecretKey);
    }

    private Optional<SECP256K1.SecretKey> createRandomSecretKey() {
      final SECP256K1.KeyPair randomKey =
          Functions.randomKeyPair(new Random(new Random().nextInt()));
      return Optional.of(randomKey.secretKey());
    }

    public Builder setNetwork(final String networkName) {
      this.networkName = networkName;
      return this;
    }

    public Builder discovery(final Consumer<DiscoveryConfig.Builder> discoveryConfigConsumer) {
      discoveryConfigConsumer.accept(discoveryConfigBuilder);
      return this;
    }

    public Builder jsonRpc(final Consumer<JsonRpcConfiguration> jsonRpcConfigurationConsumer) {
      jsonRpcConfigurationConsumer.accept(jsonRpcConfiguration);
      return this;
    }

    public Builder secretKey(final String secretKey) {
      checkNotNull(secretKey);
      final Bytes32 secretKeyInBytes = Bytes32.fromHexString(secretKey);
      this.secretKey = Optional.of(SECP256K1.SecretKey.fromBytes(secretKeyInBytes));
      return this;
    }
  }
}
