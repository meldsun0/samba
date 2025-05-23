package samba.rocksdb;

import static org.assertj.core.api.Assertions.assertThat;

import samba.rocksdb.configuration.BaseVersionedStorageFormat;
import samba.rocksdb.configuration.DatabaseMetadata;
import samba.rocksdb.exceptions.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseMetadataTest {
  @TempDir public Path temporaryFolder;

  @Test
  void readingMetadata() throws Exception {
    final Path tempDataDir =
        createAndWrite("data", "DATABASE_METADATA.json", "{\"format\":\"BASIC\",\"version\":1}");
    final DatabaseMetadata databaseMetadata = DatabaseMetadata.lookUpFrom(tempDataDir);
    assertThat(databaseMetadata.getVersionedStorageFormat())
        .isEqualTo(BaseVersionedStorageFormat.BASIC_1);
  }

  @Test
  void unsupportedMetadata() throws Exception {
    final Path tempDataDir = createAndWrite("data", "DATABASE_METADATA.json", "{\"version\":90}");
    try {
      DatabaseMetadata.lookUpFrom(tempDataDir);
    } catch (final StorageException se) {
      assertThat(se).hasMessage("Unsupported RocksDB metadata: Metadata[format=null, version=90]");
    }
  }

  private Path createAndWrite(final String dir, final String file, final String content)
      throws IOException {
    return createAndWrite(temporaryFolder, dir, file, content);
  }

  private Path createAndWrite(
      final Path temporaryFolder, final String dir, final String file, final String content)
      throws IOException {
    final Path tmpDir = temporaryFolder.resolve(dir);
    Files.createDirectories(tmpDir);
    createAndWrite(tmpDir.resolve(file), content);
    return tmpDir;
  }

  private void createAndWrite(final Path path, final String content) throws IOException {
    path.toFile().createNewFile();
    Files.writeString(path, content);
  }
}
