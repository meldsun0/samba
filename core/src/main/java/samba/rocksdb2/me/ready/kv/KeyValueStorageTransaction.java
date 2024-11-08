package samba.rocksdb2.me.ready.kv;

public interface KeyValueStorageTransaction {

    void put(Segment segment, byte[] key, byte[] value);

    void remove(Segment segment, byte[] key);

    void commit() throws StorageException;

    void rollback();
}
