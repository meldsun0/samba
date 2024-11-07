/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package samba.rocksdb;

import com.google.common.collect.ImmutableMap;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samba.config.InvalidConfigurationException;
import samba.rocksdb.exceptions.DatabaseStorageException;
import samba.rocksdb.keyvalue.KvStoreAccessor;
import samba.rocksdb.keyvalue.KvStoreConfiguration;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static samba.rocksdb.keyvalue.KvStoreConfiguration.*;


public class RocksDbInstanceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RocksDbInstanceFactory.class);

    static {
        try {
            RocksDB.loadLibrary();
        } catch (final ExceptionInInitializerError e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                LOG.info("Unable to load RocksDB library", e);
                throw new InvalidConfigurationException("Unsupported platform detected. On Windows, ensure you have 64bit Java installed.");
            } else {
                throw e;
            }
        }
    }

    public static KvStoreAccessor create(
            final MetricsSystem metricsSystem,
            final MetricCategory metricCategory,
            final KvStoreConfiguration configuration,
            final Collection<KvStoreColumn<?, ?>> columns,
            final Collection<Bytes> deletedColumns)
            throws DatabaseStorageException {
        // Track resources that need to be closed
        checkArgument(
                Stream.concat(columns.stream().map(KvStoreColumn::getId), deletedColumns.stream())
                        .distinct()
                        .count()
                        == columns.size() + deletedColumns.size(),
                "Column IDs are not distinct");

        // Create options
        final TransactionDBOptions txOptions = new TransactionDBOptions();
        final RocksDbStats rocksDbStats = new RocksDbStats(metricsSystem, metricCategory);
        final DBOptions dbOptions = createDBOptions(configuration, rocksDbStats.getStats());
        final LRUCache blockCache = new LRUCache(configuration.getCacheCapacity());
        final ColumnFamilyOptions columnFamilyOptions = createColumnFamilyOptions(configuration, blockCache);
        final List<AutoCloseable> resources = new ArrayList<>(List.of(txOptions, dbOptions, columnFamilyOptions, rocksDbStats, blockCache));

        List<ColumnFamilyDescriptor> columnDescriptors = createColumnFamilyDescriptors(columns, deletedColumns, columnFamilyOptions);
        Map<Bytes, KvStoreColumn<?, ?>> columnsById = columns.stream().collect(Collectors.toMap(KvStoreColumn::getId, Function.identity()));

        try {
            // columnHandles will be filled when the db is opened
            final List<ColumnFamilyHandle> columnHandles = new ArrayList<>(columnDescriptors.size());
            final TransactionDB db = TransactionDB.open(dbOptions, txOptions, configuration.getDatabaseDir().toString(), columnDescriptors, columnHandles);

            final ImmutableMap.Builder<KvStoreColumn<?, ?>, ColumnFamilyHandle> builder = ImmutableMap.builder();
            for (ColumnFamilyHandle columnHandle : columnHandles) {
                final Bytes columnId = Bytes.wrap(columnHandle.getName());
                final KvStoreColumn<?, ?> column = columnsById.get(columnId);
                if (column != null) {
                    // We need to check for null because the default column will not match a RocksDbColumn
                    builder.put(column, columnHandle);
                }
                resources.add(columnHandle);
            }
            final ImmutableMap<KvStoreColumn<?, ?>, ColumnFamilyHandle> columnHandlesMap = builder.build();
            final ColumnFamilyHandle defaultHandle = getDefaultHandle(columnHandles);
            resources.add(db);
            rocksDbStats.registerMetrics(db);
            return new RocksDbInstance(db, defaultHandle, columnHandlesMap, resources);
        } catch (RocksDBException e) {
            throw RocksDbExceptionUtil.wrapException(
                    "Failed to open database at path: " + configuration.getDatabaseDir(), e);
        }
    }

    private static ColumnFamilyHandle getDefaultHandle(final List<ColumnFamilyHandle> columnHandles) {
        return columnHandles.stream()
                .filter(
                        handle -> {
                            try {
                                return Bytes.wrap(handle.getName()).equals(Schema.DEFAULT_COLUMN_ID);
                            } catch (RocksDBException e) {
                                throw RocksDbExceptionUtil.wrapException(
                                        "Unable to retrieve default column handle", e);
                            }
                        })
                .findFirst()
                .orElseThrow(() -> DatabaseStorageException.unrecoverable("No default column defined"));
    }

    private static DBOptions createDBOptions(
            final KvStoreConfiguration configuration, final Statistics stats) {
        final DBOptions options =
                new DBOptions()
                        .setCreateIfMissing(true)
                        .setIncreaseParallelism(Runtime.getRuntime().availableProcessors())
                        .setMaxBackgroundJobs(configuration.getMaxBackgroundJobs())
                        .setDbWriteBufferSize(configuration.getWriteBufferCapacity())
                        .setMaxOpenFiles(configuration.getMaxOpenFiles())
                        .setBytesPerSync(1_048_576L) // 1MB
                        .setWalBytesPerSync(1_048_576L)
                        .setCreateMissingColumnFamilies(true)
                        .setLogFileTimeToRoll(TIME_TO_ROLL_LOG_FILE)
                        .setKeepLogFileNum(NUMBER_OF_LOG_FILES_TO_KEEP)
                        .setEnv(Env.getDefault().setBackgroundThreads(configuration.getBackgroundThreadCount()))
                        .setStatistics(stats);

        // Java docs suggests this if db is under 1GB, nearly impossible atm
        if (configuration.optimizeForSmallDb()) {
            options.optimizeForSmallDb();
        }

        return options;
    }

    private static ColumnFamilyOptions createColumnFamilyOptions(
            final KvStoreConfiguration configuration, final Cache cache) {
        return new ColumnFamilyOptions()
                .setCompressionType(configuration.getCompressionType())
                .setBottommostCompressionType(configuration.getBottomMostCompressionType())
                .setTtl(0)
                .setTableFormatConfig(createBlockBasedTableConfig(cache));
    }

    private static List<ColumnFamilyDescriptor> createColumnFamilyDescriptors(
            final Collection<KvStoreColumn<?, ?>> columns,
            final Collection<Bytes> deletedColumns,
            final ColumnFamilyOptions columnFamilyOptions) {
        final List<ColumnFamilyDescriptor> columnDescriptors =
                Stream.concat(columns.stream().map(KvStoreColumn::getId), deletedColumns.stream())
                        .map(id -> new ColumnFamilyDescriptor(id.toArrayUnsafe(), columnFamilyOptions))
                        .collect(Collectors.toCollection(ArrayList::new));
        columnDescriptors.add(
                new ColumnFamilyDescriptor(Schema.DEFAULT_COLUMN_ID.toArrayUnsafe(), columnFamilyOptions));
        return Collections.unmodifiableList(columnDescriptors);
    }

    private static BlockBasedTableConfig createBlockBasedTableConfig(final Cache cache) {
        return new BlockBasedTableConfig()
                .setFormatVersion(5)
                .setBlockCache(cache)
                .setFilterPolicy(new BloomFilter(10, false))
                .setPartitionFilters(true)
                .setCacheIndexAndFilterBlocks(true)
                .setBlockSize(ROCKSDB_BLOCK_SIZE);
    }
}