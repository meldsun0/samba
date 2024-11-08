package samba.rocksdb;

import org.apache.commons.lang3.tuple.Pair;
import java.io.Closeable;
import java.util.Optional;

import java.util.stream.Stream;

//SegmentdKeyValueStorage and KeyValueStorage
public interface KeyValueStorage extends Closeable {

  Optional<byte[]> get(Segment segment, byte[] key) throws StorageException;

  default boolean containsKey(final Segment segment, final byte[] key) throws StorageException {
    return get(segment, key).isPresent();
  }

  KeyValueStorageTransaction startTransaction() throws StorageException;

  boolean tryDelete(Segment segment, byte[] key) throws StorageException;

  void clear(Segment segment)  throws StorageException;

  Stream<Pair<byte[], byte[]>> stream(final Segment segment);

  Stream<byte[]> streamKeys(final Segment segment);

  boolean isClosed();

}
