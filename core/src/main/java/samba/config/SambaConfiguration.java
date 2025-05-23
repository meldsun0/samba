package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import samba.jsonrpc.config.JsonRpcConfiguration;
import samba.logging.FramedLogMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.util.Functions;

public class SambaConfiguration {
  private final MetricsConfig metricsConfig;
  private final RestServerConfig restServerConfig;
  private final DiscoveryConfig discoveryConfig;
  private final StorageConfig storageConfig;
  private final JsonRpcConfiguration jsonRpcConfiguration;
  private final StartupHardwareConfig startupHardwareConfig;
  private final SECP256K1.SecretKey secretKey;

  private boolean useDefaultBootnodes;
  private String loggingLevel;
  private String portalSubNetwork;
  private Path dataPath;

  private SambaConfiguration(
      final MetricsConfig metricsConfig,
      final RestServerConfig restServerConfig,
      final DiscoveryConfig discoveryConfig,
      final JsonRpcConfiguration jsonRpcConfiguration,
      final StorageConfig storageConfig,
      final StartupHardwareConfig startupHardwareConfig,
      final SECP256K1.SecretKey secretKey,
      boolean useDefaultBootnodes,
      final Path dataPath,
      String loggingLevel,
      String portalSubNetwork) {
    this.metricsConfig = metricsConfig;
    this.restServerConfig = restServerConfig;
    this.discoveryConfig = discoveryConfig;
    this.jsonRpcConfiguration = jsonRpcConfiguration;
    this.storageConfig = storageConfig;
    this.startupHardwareConfig = startupHardwareConfig;
    this.secretKey = secretKey;
    this.useDefaultBootnodes = useDefaultBootnodes;
    this.dataPath = dataPath;
    this.loggingLevel = loggingLevel;
    this.portalSubNetwork = portalSubNetwork;
  }

  public static Builder builder() {
    return new Builder();
  }

  public MetricsConfig getMetricsConfig() {
    return metricsConfig;
  }

  public RestServerConfig getRestServerConfig() {
    return restServerConfig;
  }

  public DiscoveryConfig getDiscoveryConfig() {
    return discoveryConfig;
  }

  public StorageConfig getStorageConfig() {
    return storageConfig;
  }

  public SECP256K1.SecretKey getSecreteKey() {
    return secretKey;
  }

  public JsonRpcConfiguration getJsonRpcConfigurationn() {
    return jsonRpcConfiguration;
  }

  public Path getDataPath() {
    return this.dataPath;
  }

  public String generateSambaConfigurationSummary(NodeRecord nodeRecord) {
    final List<String> lines = new ArrayList<>();
    lines.add("Samba version " + VersionProvider.IMPLEMENTATION_VERSION.orElse("Unknown"));
    lines.add("CommitHash " + VersionProvider.COMMIT_HASH.orElse("Unknown"));
    lines.add("Network: " + this.portalSubNetwork);
    lines.add("Logging Level: " + this.loggingLevel);
    lines.add("Bootnodes Enabled: " + this.useDefaultBootnodes);
    lines.add("Node Summary: ");
    lines.add("NodeId: " + nodeRecord.getNodeId());
    lines.add("PublicKey: " + nodeRecord.get("secp256k1"));
    lines.add("DataPath: " + this.dataPath);

    lines.addAll(this.startupHardwareConfig.getStartupSummeryLog());
    lines.addAll(this.getDiscoveryConfig().getDiscoveryConfigSummaryLog());
    lines.addAll(this.getRestServerConfig().getRestServerSummaryLog());
    lines.addAll(this.getJsonRpcConfigurationn().getJsonRpcServerSummaryLog());
    lines.addAll(this.getStorageConfig().getStorageConfigSummaryLog());
    return FramedLogMessage.generate(lines);
  }

  public static class Builder {
    private final MetricsConfig.MetricsConfigBuilder metricsConfigBuilder = MetricsConfig.builder();
    private final RestServerConfig.Builder portalRestApiConfigBuilder = RestServerConfig.builder();
    private final DiscoveryConfig.Builder discoveryConfigBuilder = DiscoveryConfig.builder();
    private final StorageConfig.Builder storageConfigBuilder = StorageConfig.builder();
    private final JsonRpcConfiguration.Builder jsonRpcConfiguration =
        JsonRpcConfiguration.builder();
    private Optional<SECP256K1.SecretKey> secretKey = Optional.empty();

    private boolean useDefaultBootnodes;
    private Path dataPath;
    private String loggingLevel;
    private String portalSubNetwork;

    private Builder() {}

    public SambaConfiguration build() {
      initMissingDefaults();

      return new SambaConfiguration(
          metricsConfigBuilder.build(),
          portalRestApiConfigBuilder.build(),
          discoveryConfigBuilder.build(),
          jsonRpcConfiguration.build(),
          storageConfigBuilder.build(),
          new StartupHardwareConfig(),
          secretKey.get(),
          this.useDefaultBootnodes,
          this.dataPath,
          this.loggingLevel,
          this.portalSubNetwork);
    }

    private void initMissingDefaults() {
      secretKey = secretKey.or(this::createRandomSecretKey);
    }

    private Optional<SECP256K1.SecretKey> createRandomSecretKey() {
      final SECP256K1.KeyPair randomKey =
          Functions.randomKeyPair(new Random(new Random().nextInt()));
      return Optional.of(randomKey.secretKey());
    }

    public Builder discovery(final Consumer<DiscoveryConfig.Builder> discoveryConfigConsumer) {
      discoveryConfigConsumer.accept(discoveryConfigBuilder);
      return this;
    }

    public Builder jsonRpc(
        final Consumer<JsonRpcConfiguration.Builder> jsonRpcConfigurationConsumer) {
      jsonRpcConfigurationConsumer.accept(jsonRpcConfiguration);
      return this;
    }

    public Builder storage(final Consumer<StorageConfig.Builder> storageConfigConsumer) {
      storageConfigConsumer.accept(storageConfigBuilder);
      return this;
    }

    public Builder restServer(final Consumer<RestServerConfig.Builder> restServerConfigConsumer) {
      restServerConfigConsumer.accept(portalRestApiConfigBuilder);
      return this;
    }

    public Builder secretKey(final String secretKey) {
      checkNotNull(secretKey);
      final Bytes32 secretKeyInBytes = Bytes32.fromHexString(secretKey);
      this.secretKey = Optional.of(SECP256K1.SecretKey.fromBytes(secretKeyInBytes));
      return this;
    }

    public Builder useDefaultBootnodes(boolean useDefaultBootnodes) {
      this.useDefaultBootnodes = useDefaultBootnodes;
      return this;
    }

    public Builder dataPath(Path dataPath) {
      this.dataPath = dataPath;
      return this;
    }

    public Builder loggingLevel(String loggingLevel) {
      this.loggingLevel = loggingLevel;
      return this;
    }

    public Builder portalSubNetwork(String portalSubNetwork) {
      this.portalSubNetwork = portalSubNetwork;
      return this;
    }
  }
}
