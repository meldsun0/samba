package samba.rocksdb;

import samba.rocksdb.exceptions.StorageException;

import java.io.Closeable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

// SegmentdKeyValueStorage and KeyValueStorage
public interface KeyValueStorage extends Closeable {

  Optional<byte[]> get(Segment segment, byte[] key) throws StorageException;

  default boolean containsKey(final Segment segment, final byte[] key) throws StorageException {
    return get(segment, key).isPresent();
  }

  KeyValueStorageTransaction startTransaction() throws StorageException;

  boolean tryDelete(Segment segment, byte[] key) throws StorageException;

  void clear(Segment segment) throws StorageException;

  Stream<Pair<byte[], byte[]>> stream(final Segment segment);

  Stream<byte[]> streamKeys(final Segment segment);

  boolean isClosed();

  Set<byte[]> getAllKeysThat(Segment segment, Predicate<byte[]> returnCondition);
}
