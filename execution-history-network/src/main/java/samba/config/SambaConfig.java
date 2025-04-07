package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.util.Functions;

public class SambaConfig {

  public static final String DEFAULT_NETWORK_NAME = "mainnet";
  private final DiscoveryConfig discoveryConfig;
  private final StorageConfig storageConfig;
  private final SECP256K1.SecretKey secretKey;

  private SambaConfig(
      final DiscoveryConfig discoveryConfig,
      final StorageConfig storageConfig,
      final SECP256K1.SecretKey secretKey) {
    this.discoveryConfig = discoveryConfig;
    this.storageConfig = storageConfig;
    this.secretKey = secretKey;
  }

  public static Builder builder() {
    return new Builder();
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

  public static class Builder {
    private final DiscoveryConfig.Builder discoveryConfigBuilder = DiscoveryConfig.builder();
    private final StorageConfig.Builder storageConfigBuilder = StorageConfig.builder();
    private Optional<SECP256K1.SecretKey> secretKey = Optional.empty();
    private String networkName;

    private Builder() {}

    public SambaConfig build() {
      initMissingDefaults();

      return new SambaConfig(
          discoveryConfigBuilder.build(), storageConfigBuilder.build(), secretKey.get());
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

    public Builder storage(final Consumer<StorageConfig.Builder> storageConfigConsumer) {
      storageConfigConsumer.accept(storageConfigBuilder);
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
