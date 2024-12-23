package samba.storage;

import static com.google.common.base.Preconditions.*;

import samba.domain.content.ContentType;
import samba.domain.content.ContentUtil;
import samba.rocksdb.*;
import samba.rocksdb.exceptions.StorageException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockWithReceipts;
import org.hyperledger.besu.plugin.services.MetricsSystem;

public class HistoryRocksDB implements HistoryDB {

  protected static final Logger LOG = LogManager.getLogger();
  private final RocksDBInstance rocksDBInstance;

  public HistoryRocksDB(
      Path path, MetricsSystem metricsSystem, RocksDBMetricsFactory rocksDBMetricsFactory)
      throws StorageException {
    this.rocksDBInstance =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(path),
            Arrays.asList(KeyValueSegment.values()),
            List.of(),
            metricsSystem,
            rocksDBMetricsFactory);
  }

  // TODO reduce the verbosity of this method once is ready.
  @Override
  public boolean saveContent(Bytes contentKey, Bytes value) {
    ContentType contentType = ContentType.fromContentKey(contentKey);
    LOG.info("Store {} with Key: {} and Value {}", contentType, contentKey, value);
    try {
      switch (contentType) {
        case ContentType.BLOCK_HEADER -> {
          Bytes blockHash = contentKey.slice(1, contentKey.size()); // blockHash is in ssz.
          // block_header_with_proof = BlockHeaderWithProof(header: rlp.encode(header), proof:
          // proof)
          if (!ContentUtil.isBlockHeaderValid(blockHash, value)) {
            LOG.info("BlockHeader for blockHash: {} is invalid", blockHash);
            break;
          }
          save(KeyValueSegment.BLOCK_HEADER, blockHash, value); // TODO async
        }
        case ContentType.BLOCK_BODY -> {
          Bytes blockHash = contentKey.slice(1, contentKey.size()); // blockHash is in ssz.
          this.getBlockHeaderByBlockHash(blockHash)
              .ifPresentOrElse(
                  blockHeader -> {
                    if (!ContentUtil.isBlockBodyValid(blockHeader, value)) {
                      save(KeyValueSegment.BLOCK_BODY, blockHash, value);
                    } else {
                      LOG.info("BlockBody for blockHash: {} is invalid", blockHash);
                    }
                  },
                  () -> {
                    // TODO trigger a lookup: Query X nearest  until either content is found or no
                    // peers are left to query.
                    LOG.info("Block Header for {} not found locally", blockHash);
                  });
        }
        case ContentType.RECEIPT -> {
          Bytes blockHashInSSZ = contentKey.slice(1, contentKey.size());
          // TODO should we do any validation?
          save(KeyValueSegment.RECEIPT, blockHashInSSZ, value);
        }
        case ContentType.BLOCK_HEADER_BY_NUMBER -> {
          Bytes blockNumberInSSZ = contentKey.slice(1, contentKey.size());
          if (!ContentUtil.isBlockHeaderValid(blockNumberInSSZ, value)) {
            LOG.info("BlockHeader for blockNumber: {} is invalid", blockNumberInSSZ);
            break;
          }
          // TODO once ssz is solve change this.
          var blockHash = Bytes.EMPTY;
          var blockNumber = Bytes.EMPTY;
          save(KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER, blockNumber, blockHash);
          save(KeyValueSegment.BLOCK_HEADER, blockHash, value);
        }
        default ->
            throw new IllegalArgumentException(
                String.format("CONTENT: Invalid payload type %s", contentType));
      }
      return true;
    } catch (Exception e) {
      LOG.info("Content could not be saved. ContentKey: {} , ContentValue{}", contentKey, value);
      return false;
    }
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockHash(Bytes blockHash) {
    Optional<byte[]> sszBlockHeader =
        this.rocksDBInstance.get(KeyValueSegment.BLOCK_HEADER, blockHash.toArray());
    return sszBlockHeader.flatMap(ContentUtil::createBlockHeaderfromSSZBytes);
  }

  @Override
  public Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumber) {
    Optional<byte[]> blockHash =
        this.rocksDBInstance.get(KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER, blockNumber.toArray());
    return blockHash.flatMap(ContentUtil::createBlockHashFromSSZBytes);
  }

  @Override
  public Optional<BlockBody> getBlockBodyByBlockHash(Bytes blockHash) {
    Optional<byte[]> sszBlockBody =
        this.rocksDBInstance.get(KeyValueSegment.BLOCK_BODY, blockHash.toArray());
    return sszBlockBody.flatMap(ContentUtil::createBlockBodyFromSSZBytes);
  }

  @Override
  public Optional<BlockWithReceipts> getBlockReceiptByBlockHash(Bytes blockHash) {
    Optional<byte[]> sszBlockHeader =
        this.rocksDBInstance.get(KeyValueSegment.RECEIPT, blockHash.toArray());
    return sszBlockHeader.flatMap(ContentUtil::createBlockWithReceiptsfromSSZBytes);
  }

  @Override
  public Optional<byte[]> get(ContentType contentType, Bytes contentKey) {
    return this.rocksDBInstance.get(getSegmentFromContentType(contentType), contentKey.toArray());
  }

  private void save(Segment segment, Bytes key, Bytes content) {
    checkArgument(
        !content.isEmpty(),
        "Content should have more than 1 byte when persisting {}",
        segment.getName());
    KeyValueStorageTransaction tx = rocksDBInstance.startTransaction();
    tx.put(segment, key.toArray(), content.toArray());
    tx.commit();
  }

  private Segment getSegmentFromContentType(ContentType contentType) {
    return switch (contentType) {
      case ContentType.BLOCK_HEADER -> KeyValueSegment.BLOCK_HEADER;
      case ContentType.BLOCK_BODY -> KeyValueSegment.BLOCK_BODY;
      case ContentType.RECEIPT -> KeyValueSegment.RECEIPT;
      case ContentType.BLOCK_HEADER_BY_NUMBER -> KeyValueSegment.BLOCK_HASH_BY_BLOCK_NUMBER;
    };
  }

  public void close() {
    this.rocksDBInstance.close();
  }
}
