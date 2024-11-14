package samba.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.domain.content.ContentType;
import samba.domain.content.ContentUtil;
import samba.storage.rocksdb.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;

public class HistoryRocksDB  implements HistoryDB {

    protected static final Logger LOG = LogManager.getLogger();

    private static final int SHANGHAI_TIMESTAMP = 1681338455;

    private RocksDBInstance rocksDBInstance;

    public HistoryRocksDB(Path path, MetricsSystem metricsSystem, RocksDBMetricsFactory rocksDBMetricsFactory) throws StorageException
    {
        this.rocksDBInstance = new RocksDBInstance(RocksDBConfiguration.createDefault(path),
                Arrays.asList(KeyValueSegment.values()),
                List.of(),
                metricsSystem,
                rocksDBMetricsFactory);
    }

    public void saveContent(Bytes contentKey, Bytes value) {
        Bytes selector = contentKey.slice(0, 1);
        ContentType contentType = ContentType.fromInt(selector.toInt());
        checkNotNull(contentType, "Invalid content type from byte: " + selector);

        LOG.info("Store {} with Key: {} and Value {}", contentType, contentKey, value);

        switch (contentType) {
            case ContentType.BLOCK_HEADER -> {
                Bytes blockHash = contentKey.slice(1, contentKey.size()); //blockHash is in ssz.
                //block_header_with_proof = BlockHeaderWithProof(header: rlp.encode(header), proof: proof)
                if(ContentUtil.isBlockHeaderValid(blockHash, value)){
                    save(KeyValueSegment.BLOCK_HEADER, blockHash, value);  //TODO async
                }else{
                    LOG.info("Fail to save BlockHeader for blockHash: {}", blockHash);
                }
            }
            case ContentType.BLOCK_BODY -> {
                Bytes blockHash = contentKey.slice(1, contentKey.size()); //blockHash is in ssz.
                this.getBlockHeader(blockHash).ifPresentOrElse(blockHeader ->{
                    if(ContentUtil.isBlockBodyValid(blockHeader, value)) {
                        save(KeyValueSegment.BLOCK_BODY, blockHash, value);
                    }else{
                        LOG.info("Fail to save BlockBody for blockHash: {}. BlockHeader is not valid", blockHash);
                    }
                }, ()->{
                    //TODO trigger a lookup: Query X nearest  until either content is found or no peers are left to query.
                    LOG.info("Block Header for {} not found locally", blockHash);
                });
            }
            case ContentType.RECEIPT -> {
                Bytes blockHash = contentKey.slice(1, contentKey.size()); //blockHash is in ssz.
                //TODO should we do any validation?
                save(KeyValueSegment.RECEIPT, blockHash, value);
            }
            case ContentType.BLOCK_HEADER_BY_NUMBER -> {
                Bytes blockNumber = contentKey.slice(1, contentKey.size()); //blockNumbergit dif is in ssz.
            }
            default ->
                    throw new RuntimeException(String.format("Creation of a Content from contentType %s is not supported", contentType)); //TODO build own runtime exception.
        }

    }

    @Override
    public Optional<BlockHeader> getBlockHeader(Bytes blockHash) {
        Optional<byte[]> sszBlockHeader = this.rocksDBInstance.get(KeyValueSegment.BLOCK_HEADER, blockHash.toArray());
        return sszBlockHeader.flatMap(ContentUtil::createBlockHeaderfromSSZBytes);

    }


    public Bytes get(Bytes key) {

        // TODO Auto-generated method stub
        return null;
    }


    public void delete(Bytes key) {
        // TODO Auto-generated method stub
    }


    public boolean contains(Bytes key) {
        // TODO Auto-generated method stub
        return false;
    }


    private void save(Segment segment, Bytes key, Bytes content) {
        checkArgument(!content.isEmpty(), "Content should have more than 1 byte when persisting {}", segment.getName());
        KeyValueStorageTransaction tx = rocksDBInstance.startTransaction();
        tx.put(segment, key.toArray(), content.toArray());
        tx.commit();
    }


    public void close() {
        this.rocksDBInstance.close();
    }
}