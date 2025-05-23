package samba.rocksdb.configuration;

import samba.rocksdb.exceptions.StorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMetadata {
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseMetadata.class);

  private static final String METADATA_FILENAME = "DATABASE_METADATA.json";
  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new Jdk8Module())
          .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
          .enable(SerializationFeature.INDENT_OUTPUT);
  private final VersionedStorageFormat versionedStorageFormat;

  private DatabaseMetadata(final VersionedStorageFormat versionedStorageFormat) {
    this.versionedStorageFormat = versionedStorageFormat;
  }

  public static DatabaseMetadata defaultForNewDb(
      final VersionedStorageFormat versionedStorageFormat) {
    return new DatabaseMetadata(versionedStorageFormat);
  }

  public VersionedStorageFormat getVersionedStorageFormat() {
    return versionedStorageFormat;
  }

  public static DatabaseMetadata lookUpFrom(final Path dataDir) throws IOException {
    LOG.info("Lookup database metadata file in data directory: {}", dataDir.toString());
    return resolveDatabaseMetadata(getDefaultMetadataFile(dataDir));
  }

  public static boolean isPresent(final Path dataDir) throws IOException {
    return getDefaultMetadataFile(dataDir).exists();
  }

  public void writeToDirectory(final Path dataDir) throws IOException {
    writeToFile(getDefaultMetadataFile(dataDir));
  }

  private void writeToFile(final File file) throws IOException {
    MAPPER.writeValue(
        file,
        new Metadata(versionedStorageFormat.getFormat(), versionedStorageFormat.getVersion()));
  }

  private static File getDefaultMetadataFile(final Path dataDir) {
    return dataDir.resolve(METADATA_FILENAME).toFile();
  }

  private static DatabaseMetadata resolveDatabaseMetadata(final File metadataFile)
      throws IOException {
    try {
      final Metadata metadata = MAPPER.readValue(metadataFile, Metadata.class);
      VersionedStorageFormat versionedStorageFormat =
          Arrays.stream(BaseVersionedStorageFormat.values())
              .filter(
                  vsf ->
                      vsf.getFormat().equals(metadata.format())
                          && vsf.getVersion() == metadata.version())
              .findFirst()
              .orElseThrow(
                  () -> {
                    final String message = "Unsupported RocksDB metadata: " + metadata;
                    LOG.error(message);
                    return new StorageException(message);
                  });

      return new DatabaseMetadata(versionedStorageFormat);

    } catch (FileNotFoundException fnfe) {
      throw new StorageException(
          "Database exists but metadata file "
              + metadataFile.toString()
              + " not found, without it there is no safe way to open the database",
          fnfe);
    } catch (JsonProcessingException jpe) {
      throw new IllegalStateException(
          String.format("Invalid metadata file %s", metadataFile.getAbsolutePath()), jpe);
    }
  }

  @Override
  public String toString() {
    return "versionedStorageFormat=" + versionedStorageFormat;
  }

  @JsonSerialize
  @SuppressWarnings("unused")
  private record Metadata(DataStorageFormat format, int version) {}
}
