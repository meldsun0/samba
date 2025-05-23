package samba.rocksdb;

import samba.rocksdb.configuration.BaseVersionedStorageFormat;
import samba.rocksdb.configuration.DataStorageFormat;
import samba.rocksdb.configuration.DatabaseMetadata;
import samba.rocksdb.configuration.VersionedStorageFormat;
import samba.rocksdb.exceptions.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBKeyValueStorageFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RocksDBKeyValueStorageFactory.class);
  private static final EnumSet<BaseVersionedStorageFormat> SUPPORTED_VERSIONED_FORMATS =
      EnumSet.of(BaseVersionedStorageFormat.BASIC_1);

  private final MetricsSystem metricsSystem;

  private RocksDBKeyValueStorageFactory(final MetricsSystem metricsSystem) {
    this.metricsSystem = metricsSystem;
  }

  public static RocksDBInstance create(
      final Path dataPath,
      final Path databasePath,
      final DataStorageFormat databaseStorageFormat,
      final MetricsSystem metricsSystem) {
    return new RocksDBKeyValueStorageFactory(metricsSystem)
        .create(dataPath, databasePath, databaseStorageFormat);
  }

  private RocksDBInstance create(
      final Path dataPath, final Path databasePath, final DataStorageFormat databaseStorageFormat) {
    try {
      validateMetadata(dataPath, databasePath, databaseStorageFormat);
      return new RocksDBInstance(
          RocksDBConfiguration.createDefault(databasePath),
          Arrays.asList(KeyValueSegment.values()),
          List.of(),
          metricsSystem,
          RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);

    } catch (final IOException e) {
      final String message =
          String.format("Failed to retrieve RocksDB metadata at %s: %s", dataPath, e.getMessage());
      throw new StorageException(message, e);
    }
  }

  private void validateMetadata(
      final Path dataPath, final Path databasePath, final DataStorageFormat databaseStorageFormat)
      throws IOException {
    final boolean dataDirExists = dataPath.toFile().exists();
    final boolean databaseExists = databasePath.toFile().exists();
    final boolean metadataExists = DatabaseMetadata.isPresent(dataPath);
    DatabaseMetadata metadata;
    if (databaseExists && !metadataExists) {
      throw new StorageException(
          "Database exists but metadata file not found, without it there is no safe way to open the database");
    }
    final BaseVersionedStorageFormat runtimeVersionedStorageFormat =
        BaseVersionedStorageFormat.defaultForNewDB(databaseStorageFormat);

    if (metadataExists) {
      metadata = DatabaseMetadata.lookUpFrom(dataPath);

      if (!metadata.getVersionedStorageFormat().getFormat().equals(databaseStorageFormat)) {
        handleFormatMismatch(databaseStorageFormat, dataPath, metadata);
      }
      LOG.info("Existing database at {}. Metadata {}. Processing WAL...", dataPath, metadata);
    } else {

      metadata = DatabaseMetadata.defaultForNewDb(runtimeVersionedStorageFormat);
      LOG.info(
          "No existing database at {}. Using default metadata for new db {}", dataPath, metadata);
      if (!dataDirExists) {
        Files.createDirectories(dataPath);
      }
      metadata.writeToDirectory(dataPath);
    }

    if (!isSupportedVersionedFormat(metadata.getVersionedStorageFormat())) {
      final String message = "Unsupported RocksDB metadata: " + metadata;
      LOG.error(message);
      throw new StorageException(message);
    }
  }

  private static void handleFormatMismatch(
      final DataStorageFormat databaseStorageFormat,
      final Path dataDir,
      final DatabaseMetadata existingMetadata) {
    String error =
        String.format(
            "Database format mismatch: DB at %s is %s but config expects %s. "
                + "Please check your config.",
            dataDir,
            existingMetadata.getVersionedStorageFormat().getFormat().name(),
            databaseStorageFormat);

    throw new StorageException(error);
  }

  private boolean isSupportedVersionedFormat(final VersionedStorageFormat versionedStorageFormat) {
    return SUPPORTED_VERSIONED_FORMATS.stream()
        .anyMatch(
            vsf ->
                vsf.getFormat().equals(versionedStorageFormat.getFormat())
                    && vsf.getVersion() == versionedStorageFormat.getVersion());
  }
}
