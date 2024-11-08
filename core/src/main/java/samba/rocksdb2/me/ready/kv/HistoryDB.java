/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.rocksdb2.me.ready.kv;

import org.hyperledger.besu.plugin.services.MetricsSystem;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDB;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.rocksdb2.RocksDBConfiguration;
import samba.rocksdb2.RocksDBMetricsFactory;
import java.util.List;


//merge de TransactionDBRocksDBColumnarKeyValueStorage y SegmentedKeyValueStorageAdapter
//revisar tb TransactionDBRocksDBColumnarKeyValueStorage
public class HistoryDB extends RocksDBStorage {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryDB.class);

    //private final SegmentIdentifier segmentIdentifier;  //tiene un formato

    private final TransactionDB db;


    public HistoryDB(
            final RocksDBConfiguration configuration,
            final List<Segment> segments,
            final List<Segment> ignorableSegments,
            final MetricsSystem metricsSystem,
            final RocksDBMetricsFactory rocksDBMetricsFactory)
            throws StorageException {
        super(configuration, segments, ignorableSegments, metricsSystem, rocksDBMetricsFactory);
        try {
            db = TransactionDB.open(rocksDBOptions, rocksDBTxOptions, configuration.getDatabaseDir().toString(), columnDescriptors, columnHandles);
            initMetrics();
            initColumnHandles();

        } catch (final RocksDBException e) {
            throw parseRocksDBException(e, segments, ignorableSegments);
        }
    }

    @Override
    public KeyValueStorageTransaction startTransaction() throws StorageException {
        throwIfClosed();
        final WriteOptions writeOptions = new WriteOptions();
        writeOptions.setIgnoreMissingColumnFamilies(true);
        return new RocksDBTransaction(this::safeColumnHandle, db.beginTransaction(writeOptions), writeOptions, metrics, this.closed::get);
    }


    @Override
    RocksDB getDB() {
        return db;
    }
}
