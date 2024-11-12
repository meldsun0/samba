package samba.rocksdb;

import org.apache.commons.lang3.tuple.Pair;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.storage.SegmentIdentifier;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDB;
import org.rocksdb.WriteOptions;

import java.util.List;

public class RocksDBInstance extends RocksDBStorage {

    private final TransactionDB transactionDB;


    public RocksDBInstance(RocksDBConfiguration configuration, List<Segment> defaultSegments, List<Segment> ignorableSegments, MetricsSystem metricsSystem, RocksDBMetricsFactory rocksDBMetricsFactory) {
        super(configuration, defaultSegments, ignorableSegments, metricsSystem, rocksDBMetricsFactory);
        try {
            this.transactionDB = TransactionDB.open(rocksDBOptions, rocksDBTxOptions, configuration.databaseDir().toString(), columnDescriptors, columnHandles);
            initMetrics();
            initColumnHandles();

        } catch (final RocksDBException e) {
            throw parseRocksDBException(e, defaultSegments, ignorableSegments);
        }
    }


    RocksDB getDB() {
        return transactionDB;
    }

    @Override
    public KeyValueStorageTransaction startTransaction() throws StorageException {
        throwIfClosed();
        final WriteOptions writeOptions = new WriteOptions();
        writeOptions.setIgnoreMissingColumnFamilies(true);
        return new RocksDBTransaction(this::safeColumnHandle, transactionDB.beginTransaction(writeOptions), writeOptions, metrics, this.closed::get);
    }

}
