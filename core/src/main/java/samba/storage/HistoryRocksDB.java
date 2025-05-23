package samba.storage;

import static com.google.common.base.Preconditions.checkArgument;

import samba.config.StorageConfig;
import samba.domain.content.ContentBlockBody;
import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentReceipts;
import samba.domain.content.ContentType;
import samba.domain.content.ContentUtil;
import samba.rocksdb.KeyValueSegment;
import samba.rocksdb.KeyValueStorageTransaction;
import samba.rocksdb.RocksDBInstance;
import samba.rocksdb.RocksDBKeyValueStorageFactory;
import samba.rocksdb.Segment;
import samba.validation.util.ValidationUtil;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryRocksDB implements HistoryDB {

  private static final Logger LOG = LoggerFactory.getLogger(HistoryRocksDB.class);
  private final RocksDBInstance rocksDBInstance;

  public HistoryRocksDB(RocksDBInstance rocksDBInstance) {
    this.rocksDBInstance = rocksDBInstance;
  }

  public HistoryRocksDB(
      final Path dataPath, final StorageConfig storageConfig, MetricsSystem metricsSystem) {
    this.rocksDBInstance =
        RocksDBKeyValueStorageFactory.create(
            dataPath,
            storageConfig.getDatabasePath(),
            storageConfig.getDatabaseFormat(),
            metricsSystem);
  }

  @Override
  public boolean saveContent(Bytes sszKey, Bytes sszValue) {
    try {
      ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(sszKey).get();
      switch (contentKey.getContentType()) {
        case ContentType.BLOCK_HEADER -> {
          Bytes blockHashKeySSZ = contentKey.getBlockHashSsz();
          Optional<ContentBlockHeader> blockHeader =
              ContentUtil.createBlockHeaderfromSszBytes(sszValue);
          if (blockHeader.isPresent() && ValidationUtil.isBlockHeaderValid(blockHeader.get())) {
            Bytes blockNumberKeySSZ = ContentUtil.createBlockNumberInSSZ(blockHeader.get());
            save(KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER, blockNumberKeySSZ, blockHashKeySSZ);
            save(KeyValueSegment.BLOCK_HEADER, blockHashKeySSZ, sszValue); // TODO async
          } else {
            LOG.error("BlockHeader for blockHashKey: {} is invalid", blockHashKeySSZ);
          }
        }
        case ContentType.BLOCK_BODY -> {
          Bytes blockHashSSZ = contentKey.getBlockHashSsz();
          save(KeyValueSegment.BLOCK_BODY, blockHashSSZ, sszValue);
        }
        case ContentType.RECEIPT -> {
          Bytes blockHashSSZ = contentKey.getBlockHashSsz();
          save(KeyValueSegment.RECEIPT, blockHashSSZ, sszValue);
        }
        case ContentType.BLOCK_HEADER_BY_NUMBER -> {
          Bytes blockNumberKeySSZ = contentKey.getBlockNumberSsz();
          Optional<ContentBlockHeader> blockHeader =
              ContentUtil.createBlockHeaderfromSszBytes(sszValue);
          if (blockHeader.isPresent() && ValidationUtil.isBlockHeaderValid(blockHeader.get())) {
            Bytes blockHashKeySSZ = ContentUtil.createBlockHashKey(blockHeader.get());
            save(KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER, blockNumberKeySSZ, blockHashKeySSZ);
            save(KeyValueSegment.BLOCK_HEADER, blockHashKeySSZ, sszValue);
          } else {
            LOG.error("BlockHeader for blockNumberKey: {} is invalid", blockNumberKeySSZ);
          }
        }
        default ->
            throw new IllegalArgumentException(
                String.format("CONTENT: Invalid payload type %s", contentKey.getContentType()));
      }
      return true;
    } catch (Exception e) {
      LOG.error(
          "Content could not be saved. ContentKey: {} , ContentValue: {}. Reason:",
          sszKey,
          sszValue,
          e);
      return false;
    }
  }

  @Override
  public Optional<ContentBlockHeader> getBlockHeaderByBlockHash(Bytes blockHash) {
    Optional<byte[]> databaseBlockHeader =
        this.rocksDBInstance.get(KeyValueSegment.BLOCK_HEADER, blockHash.toArray());
    if (databaseBlockHeader.isEmpty()) return Optional.empty();
    return ContentUtil.createBlockHeaderfromSszBytes(Bytes.wrap(databaseBlockHeader.get()));
  }

  @Override
  public Optional<ContentBlockBody> getBlockBodyByBlockHash(Bytes blockHash) {
    Optional<byte[]> databaseBlockBody =
        this.rocksDBInstance.get(KeyValueSegment.BLOCK_BODY, blockHash.toArray());
    if (databaseBlockBody.isEmpty()) return Optional.empty();
    return ContentUtil.createBlockBodyFromSszBytes(Bytes.wrap(databaseBlockBody.get()));
  }

  @Override
  public Optional<ContentReceipts> getReceiptsByBlockHash(Bytes blockHash) {
    Optional<byte[]> databaseReceipts =
        this.rocksDBInstance.get(KeyValueSegment.RECEIPT, blockHash.toArray());
    if (databaseReceipts.isEmpty()) return Optional.empty();
    return ContentUtil.createReceiptsFromSszBytes(Bytes.wrap(databaseReceipts.get()));
  }

  @Override
  public Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumberKey) {
    Optional<byte[]> databaseKey =
        this.rocksDBInstance.get(
            KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER, blockNumberKey.toArray());
    if (databaseKey.isEmpty()) return Optional.empty();
    return Optional.of(Bytes.wrap(databaseKey.get()));
  }

  @Override
  public Optional<Bytes> get(ContentKey contentKey) {
    return switch (contentKey.getContentType()) {
      case ContentType.BLOCK_HEADER, ContentType.BLOCK_BODY, ContentType.RECEIPT -> {
        Bytes blockHashSSZ = contentKey.getBlockHashSsz();
        yield this.rocksDBInstance
            .get(getSegmentFromContentType(contentKey.getContentType()), blockHashSSZ.toArray())
            .map(Bytes::wrap)
            .or(Optional::empty);
      }
      case ContentType.BLOCK_HEADER_BY_NUMBER -> {
        Bytes blockNumberSSZ = contentKey.getBlockNumberSsz();
        yield this.rocksDBInstance
            .get(
                getSegmentFromContentType(ContentType.BLOCK_HEADER_BY_NUMBER),
                blockNumberSSZ.toArray())
            .flatMap(
                blockHash ->
                    this.rocksDBInstance.get(
                        getSegmentFromContentType(ContentType.BLOCK_HEADER), blockHash))
            .map(Bytes::wrap)
            .or(Optional::empty);
      }
      case ContentType.EPHEMERAL_BLOCK_HEADER -> {
        // TODO unsupported
        yield Optional.empty();
      }
    };
  }

  @Override
  public boolean isAvailable() {
    return !this.rocksDBInstance.isClosed();
  }

  private void save(Segment segment, Bytes key, Bytes content) {
    checkArgument(
        !key.isEmpty(), "Key should have more than 1 byte when persisting {}", segment.getName());
    KeyValueStorageTransaction tx = rocksDBInstance.startTransaction();
    tx.put(segment, key.toArray(), content.toArray());
    tx.commit();
    LOG.debug(
        "Saving on segment {}, Key: {} and Value: {} ",
        segment.getName(),
        key.toHexString(),
        content.toHexString());
  }

  private Segment getSegmentFromContentType(ContentType contentType) {
    return switch (contentType) {
      case ContentType.BLOCK_HEADER -> KeyValueSegment.BLOCK_HEADER;
      case ContentType.BLOCK_BODY -> KeyValueSegment.BLOCK_BODY;
      case ContentType.RECEIPT -> KeyValueSegment.RECEIPT;
      case ContentType.BLOCK_HEADER_BY_NUMBER -> KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER;
      case ContentType.EPHEMERAL_BLOCK_HEADER -> KeyValueSegment.EPHEMERAL_BLOCK_HEADER;
    };
  }

  public void close() {
    this.rocksDBInstance.close();
  }
}
