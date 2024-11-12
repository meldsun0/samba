package samba.storage;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.domain.content.ContentType;
import samba.storage.rocksdb.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class HistoryRocksDB  implements HistoryDB {

    private RocksDBInstance rocksDBInstance;

    public HistoryRocksDB(Path path, MetricsSystem metricsSystem, RocksDBMetricsFactory rocksDBMetricsFactory) throws StorageException {
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
        switch (contentType) {
            case ContentType.BLOCK_HEADER -> {
                //   validateHeader
                //               save
            }
            case ContentType.BLOCK_BODY -> {
                Bytes blockHash = SSZ.decodeBytes(contentKey.slice(1, contentKey.size()));
                saveBlockBody(blockHash, value);
            }
            case ContentType.RECEIPT -> {
            }
            case ContentType.BLOCK_HEADER_BY_NUMBER -> {
            }
            default ->
                    throw new RuntimeException(String.format("Creation of a Content from contentType %s is not supported", contentType)); //TODO build own runtime exception.
        }
        ;
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


    private void saveBlockBody(Bytes blockHash, Bytes content) {
        checkArgument(!content.isEmpty(), "Content should have more than 1 byte when persisting BlockBody");
        KeyValueStorageTransaction tx = rocksDBInstance.startTransaction();
        tx.put(KeyValueSegment.BLOCK_BODY, blockHash.toArray(), content.toArray());
        tx.commit();
    }

    public void close() {
        this.rocksDBInstance.close();
    }
}