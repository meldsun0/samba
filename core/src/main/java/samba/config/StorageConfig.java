package samba.config;

public class StorageConfig {

  public static final long DEFAULT_STORAGE_FREQUENCY = 2048L;
  private final long dataStorageFrequency;

  public StorageConfig(final long dataStorageFrequency) {
    this.dataStorageFrequency = dataStorageFrequency;
  }

  public long getDataStorageFrequency() {
    return dataStorageFrequency;
  }
}
