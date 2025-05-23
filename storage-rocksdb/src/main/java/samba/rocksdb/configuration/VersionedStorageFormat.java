package samba.rocksdb.configuration;

public interface VersionedStorageFormat {

  DataStorageFormat getFormat();

  int getVersion();
}
