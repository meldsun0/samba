package samba.rocksdb.configuration;

public enum BaseVersionedStorageFormat implements VersionedStorageFormat {
  BASIC_1(DataStorageFormat.BASIC, 1);

  private final DataStorageFormat format;
  private final int version;

  BaseVersionedStorageFormat(final DataStorageFormat format, final int version) {
    this.format = format;
    this.version = version;
  }

  @Override
  public DataStorageFormat getFormat() {
    return format;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return "BaseVersionedStorageFormat{" + "format=" + format + ", version=" + version + '}';
  }

  public static BaseVersionedStorageFormat defaultForNewDB(final DataStorageFormat storageFormat) {
    return switch (storageFormat) {
      case BASIC -> BASIC_1;
    };
  }
}
