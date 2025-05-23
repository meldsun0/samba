package samba.rocksdb;

import static org.assertj.core.api.Assertions.*;

import samba.rocksdb.configuration.DataStorageFormat;
import samba.rocksdb.exceptions.StorageException;

import java.nio.file.Files;
import java.nio.file.Path;

import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RocksDBKeyValueStorageFactoryTest {

  @Test
  void createShouldCreateDefaultMetadataIfNoneExists(@TempDir Path tempDir) throws Exception {
    Path dataPath = tempDir.resolve("data");
    Path dbPath = tempDir.resolve("db");

    MetricsSystem metricsSystem = new NoOpMetricsSystem();
    RocksDBInstance instance =
        RocksDBKeyValueStorageFactory.create(
            dataPath, dbPath, DataStorageFormat.BASIC, metricsSystem);

    assertThat(instance).isNotNull();
    assertThat(Files.exists(dataPath.resolve("DATABASE_METADATA.json"))).isTrue();
  }

  @Test
  void shouldFailIfDBExistsAndMetadataIsMissing(@TempDir Path tempDir) throws Exception {
    Path dataPath = tempDir.resolve("data");
    Path dbPath = tempDir.resolve("db");
    Files.createDirectories(dataPath);
    Files.createDirectories(dbPath);
    MetricsSystem metricsSystem = new NoOpMetricsSystem();

    assertThatThrownBy(
            () ->
                RocksDBKeyValueStorageFactory.create(
                    dataPath, dbPath, DataStorageFormat.BASIC, metricsSystem))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("metadata file not found");
  }

  @Test
  void shouldThrowOnUnsafeDowngrade(@TempDir Path tempDir) throws Exception {
    Path dataPath = tempDir.resolve("data");
    Files.createDirectories(dataPath);
    Path dbPath = tempDir.resolve("db");
    Files.createDirectories(dbPath);

    tempDir.resolve("DATABASE_METADATA.json").toFile().createNewFile();
    Files.writeString(
        dataPath.resolve("DATABASE_METADATA.json"), "{\"format\":\"BASIC\",\"version\":2}");

    MetricsSystem metricsSystem = new NoOpMetricsSystem();

    assertThatThrownBy(
            () ->
                RocksDBKeyValueStorageFactory.create(
                    dataPath, dbPath, DataStorageFormat.BASIC, metricsSystem))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("Unsupported RocksDB metadata");
  }
}
