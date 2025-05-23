package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import samba.rocksdb.configuration.DataStorageFormat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StorageConfig {
  public static final String DATABASE_PATH_NAME = "database";
  private final Path databasePath;
  private final DataStorageFormat dataStorageFormat = DataStorageFormat.BASIC;

  private StorageConfig(final Path databasePath) {
    this.databasePath = databasePath;
  }

  public static Builder builder() {
    return new Builder();
  }

  public DataStorageFormat getDatabaseFormat() {
    return this.dataStorageFormat;
  }

  public Path getDatabasePath() {
    return this.databasePath;
  }

  public static class Builder {

    private Path databasePath;

    private Builder() {}

    public StorageConfig build() {
      return new StorageConfig(databasePath);
    }

    public Builder databasePath(final Path databasePath) {
      checkNotNull(databasePath);
      this.databasePath = databasePath.resolve(DATABASE_PATH_NAME);
      return this;
    }
  }

  public List<String> getStorageConfigSummaryLog() {
    List<String> summary = new ArrayList<>();
    summary.add("Storage Summary:");
    summary.add("DATABSE PATH: " + this.databasePath);
    return summary;
  }
}
