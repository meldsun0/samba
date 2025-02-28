/*
 * Copyright contributors to Hyperledger Besu.
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
package samba.rocksdb;

import static java.util.stream.Collectors.toUnmodifiableSet;

import samba.rocksdb.exceptions.InvalidConfigurationException;
import samba.rocksdb.exceptions.StorageException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.Env;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Statistics;
import org.rocksdb.Status;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RocksDBStorage implements KeyValueStorage {

  private static final Logger LOG = LoggerFactory.getLogger(RocksDBStorage.class);
  private static final int ROCKSDB_FORMAT_VERSION = 5;
  private static final long ROCKSDB_BLOCK_SIZE = 32768;

  protected static final long ROCKSDB_BLOCKCACHE_SIZE_WHEN_USING_HIGH_SPEC_OPT = 1_073_741_824L;
  protected static final long ROCKSDB_MEMTABLE_SIZE_WHEN_USING_HIGH_SPEC_OPT = 536_870_912L;
  protected static final long WAL_MAX_TOTAL_SIZE_AFTER_WHICH_A_FLUSH_IS_TRIGGERED = 1_073_741_824L;
  protected static final long EXPECTED_WAL_FILE_SIZE_TO_KEEP_AROUND = 67_108_864L;
  private static final long NUMBER_OF_LOG_FILES_TO_KEEP_ON_DISK = 7;

  private static final long TIME_TO_ROLL_LOG_FILE_IN_SECONDS =
      86_400L; // (1 day = 3600 * 24 seconds)

  static {
    try {
      RocksDB.loadLibrary();
    } catch (final ExceptionInInitializerError e) {
      if (e.getCause() instanceof UnsupportedOperationException) {
        LOG.info("Unable to load RocksDB library", e);
        throw new InvalidConfigurationException(
            "Unsupported platform detected. On Windows, ensure you have 64bit Java installed.");
      } else {
        throw e;
      }
    }
  }

  /** atomic boolean to track if the storage is closed */
  protected final AtomicBoolean closed = new AtomicBoolean(false);

  private final WriteOptions tryDeleteOptions =
      new WriteOptions().setNoSlowdown(true).setIgnoreMissingColumnFamilies(true);
  private final ReadOptions readOptions = new ReadOptions().setVerifyChecksums(false);
  private final MetricsSystem metricsSystem;
  private final RocksDBMetricsFactory rocksDBMetricsFactory;
  private final RocksDBConfiguration configuration;

  protected DBOptions rocksDBOptions;
  protected TransactionDBOptions rocksDBTxOptions;
  protected final Statistics stats = new Statistics();
  protected RocksDBMetrics metrics;
  protected Map<Segment, RocksDBSegmentIdentifier> columnHandlesBySegmentIdentifier;
  protected List<ColumnFamilyDescriptor> columnDescriptors;
  protected List<ColumnFamilyHandle> columnHandles;
  protected List<Segment> trimmedSegments;

  public RocksDBStorage(
      final RocksDBConfiguration configuration,
      final List<Segment> defaultSegments,
      final List<Segment> ignorableSegments,
      final MetricsSystem metricsSystem,
      final RocksDBMetricsFactory rocksDBMetricsFactory)
      throws StorageException {

    this.configuration = configuration;
    this.metricsSystem = metricsSystem;
    this.rocksDBMetricsFactory = rocksDBMetricsFactory;

    try {
      trimmedSegments = new ArrayList<>(defaultSegments);
      final List<byte[]> existingColumnFamilies =
          RocksDB.listColumnFamilies(new Options(), configuration.databaseDir().toString());
      // Only ignore if not existed currently
      ignorableSegments.stream()
          .filter(
              ignorableSegment ->
                  existingColumnFamilies.stream()
                      .noneMatch(existed -> Arrays.equals(existed, ignorableSegment.getId())))
          .forEach(trimmedSegments::remove);
      columnDescriptors =
          trimmedSegments.stream()
              .map(segment -> createColumnDescriptor(segment, configuration))
              .collect(Collectors.toList());

      setGlobalOptions(configuration, stats);

      rocksDBTxOptions = new TransactionDBOptions();
      columnHandles = new ArrayList<>(columnDescriptors.size());

    } catch (RocksDBException e) {
      throw parseRocksDBException(e, defaultSegments, ignorableSegments);
    }
  }

  abstract RocksDB getDB();

  /**
   * Create a Column Family Descriptor for a given segment It defines basically the different
   * options to apply to the corresponding Column Family
   *
   * @param segment the segment identifier
   * @param configuration RocksDB configuration
   * @return a column family descriptor
   */
  private ColumnFamilyDescriptor createColumnDescriptor(
      final Segment segment, final RocksDBConfiguration configuration) {

    BlockBasedTableConfig basedTableConfig = createBlockBasedTableConfig(segment, configuration);

    final var options =
        new ColumnFamilyOptions()
            .setTtl(0)
            .setCompressionType(CompressionType.LZ4_COMPRESSION)
            .setTableFormatConfig(basedTableConfig);

    if (segment.containsStaticData()) {
      options
          .setEnableBlobFiles(true)
          .setEnableBlobGarbageCollection(segment.isStaticDataGarbageCollectionEnabled())
          .setMinBlobSize(100)
          .setBlobCompressionType(CompressionType.LZ4_COMPRESSION);
    }

    return new ColumnFamilyDescriptor(segment.getId(), options);
  }

  /***
   * Create a Block Base Table configuration for each segment, depending on the configuration in place
   * and the segment itself
   *
   * @param segment The segment related to the column family
   * @param config RocksDB configuration
   * @return Block Base Table configuration
   */
  private BlockBasedTableConfig createBlockBasedTableConfig(
      final Segment segment, final RocksDBConfiguration config) {
    final LRUCache cache =
        new LRUCache(
            config.isHighSpec() && segment.isEligibleToHighSpecFlag()
                ? ROCKSDB_BLOCKCACHE_SIZE_WHEN_USING_HIGH_SPEC_OPT
                : config.cacheCapacity());
    return new BlockBasedTableConfig()
        .setFormatVersion(ROCKSDB_FORMAT_VERSION)
        .setBlockCache(cache)
        .setFilterPolicy(new BloomFilter(10, false))
        .setPartitionFilters(true)
        .setCacheIndexAndFilterBlocks(false)
        .setBlockSize(ROCKSDB_BLOCK_SIZE);
  }

  /***
   * Set Global options (DBOptions)
   *
   * @param configuration RocksDB configuration
   * @param stats The statistics object
   */
  private void setGlobalOptions(final RocksDBConfiguration configuration, final Statistics stats) {
    rocksDBOptions = new DBOptions();
    rocksDBOptions
        .setCreateIfMissing(true)
        .setMaxOpenFiles(configuration.maxOpenFiles())
        .setStatistics(stats)
        .setCreateMissingColumnFamilies(true)
        .setLogFileTimeToRoll(TIME_TO_ROLL_LOG_FILE_IN_SECONDS)
        .setKeepLogFileNum(NUMBER_OF_LOG_FILES_TO_KEEP_ON_DISK)
        .setEnv(Env.getDefault().setBackgroundThreads(configuration.backgroundThreadCount()))
        .setMaxTotalWalSize(WAL_MAX_TOTAL_SIZE_AFTER_WHICH_A_FLUSH_IS_TRIGGERED)
        .setRecycleLogFileNum(
            WAL_MAX_TOTAL_SIZE_AFTER_WHICH_A_FLUSH_IS_TRIGGERED
                / EXPECTED_WAL_FILE_SIZE_TO_KEEP_AROUND);
  }

  /**
   * Parse RocksDBException and wrap in StorageException
   *
   * @param ex RocksDBException
   * @param defaultSegments segments requested to open
   * @param ignorableSegments segments which are ignorable if not present
   * @return StorageException wrapping the RocksDB Exception
   */
  protected static StorageException parseRocksDBException(
      final RocksDBException ex,
      final List<Segment> defaultSegments,
      final List<Segment> ignorableSegments) {
    String message = ex.getMessage();
    List<Segment> knownSegments =
        Streams.concat(defaultSegments.stream(), ignorableSegments.stream()).distinct().toList();

    // parse out unprintable segment names for a more useful exception:
    String columnExceptionMessagePrefix = "Column families not opened: ";
    if (message.contains(columnExceptionMessagePrefix)) {
      String substring = message.substring(message.indexOf(": ") + 2);

      List<String> unHandledSegments = new ArrayList<>();
      Splitter.on(", ")
          .splitToStream(substring)
          .forEach(
              part -> {
                byte[] bytes = part.getBytes(StandardCharsets.UTF_8);
                unHandledSegments.add(
                    knownSegments.stream()
                        .filter(seg -> Arrays.equals(seg.getId(), bytes))
                        .findFirst()
                        .map(seg -> new SegmentRecord(seg.getName(), seg.getId()))
                        .orElse(new SegmentRecord(part, bytes))
                        .forDisplay());
              });

      return new StorageException(
          "RocksDBException: Unhandled column families: ["
              + unHandledSegments.stream().collect(Collectors.joining(", "))
              + "]");
    } else {
      return new StorageException(ex);
    }
  }

  void initMetrics() {
    metrics = rocksDBMetricsFactory.create(metricsSystem, configuration, getDB(), stats);
  }

  void initColumnHandles() throws RocksDBException {
    // will not include the DEFAULT columnHandle, we do not use it:
    columnHandlesBySegmentIdentifier =
        trimmedSegments.stream()
            .collect(
                Collectors.toMap(
                    segmentId -> segmentId,
                    segment -> {
                      var columnHandle =
                          columnHandles.stream()
                              .filter(
                                  ch -> {
                                    try {
                                      return Arrays.equals(ch.getName(), segment.getId());
                                    } catch (RocksDBException e) {
                                      throw new RuntimeException(e);
                                    }
                                  })
                              .findFirst()
                              .orElseThrow(
                                  () ->
                                      new RuntimeException(
                                          "Column handle not found for segment "
                                              + segment.getName()));
                      return new RocksDBSegmentIdentifier(getDB(), columnHandle);
                    }));
  }

  /**
   * Safe method to map segment identifier to column handle.
   *
   * @param segment segment identifier
   * @return column handle
   */
  protected ColumnFamilyHandle safeColumnHandle(final Segment segment) {
    RocksDBSegmentIdentifier safeRef = columnHandlesBySegmentIdentifier.get(segment);
    if (safeRef == null) {
      throw new RuntimeException("Column handle not found for segment " + segment.getName());
    }
    return safeRef.get();
  }

  @Override
  public Optional<byte[]> get(final Segment segment, final byte[] key) throws StorageException {
    throwIfClosed();
    try (final OperationTimer.TimingContext ignored = metrics.readLatency().startTimer()) {
      return Optional.ofNullable(getDB().get(safeColumnHandle(segment), readOptions, key));
    } catch (final RocksDBException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public Stream<Pair<byte[], byte[]>> stream(final Segment segment) {
    final RocksIterator rocksIterator = getDB().newIterator(safeColumnHandle(segment));
    rocksIterator.seekToFirst();
    return RocksDBIterator.create(rocksIterator).toStream();
  }

  @Override
  public Stream<byte[]> streamKeys(final Segment segment) {
    final RocksIterator rocksIterator = getDB().newIterator(safeColumnHandle(segment));
    rocksIterator.seekToFirst();
    return RocksDBIterator.create(rocksIterator).toStreamKeys();
  }

  @Override
  public boolean tryDelete(final Segment segment, final byte[] key) {
    try {
      getDB().delete(safeColumnHandle(segment), tryDeleteOptions, key);
      return true;
    } catch (RocksDBException e) {
      if (e.getStatus().getCode() == Status.Code.Incomplete) {
        return false;
      } else {
        throw new StorageException(e);
      }
    }
  }

  @Override
  public void clear(final Segment segment) {
    Optional.ofNullable(columnHandlesBySegmentIdentifier.get(segment))
        .ifPresent(RocksDBSegmentIdentifier::reset);
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      rocksDBTxOptions.close();
      rocksDBOptions.close();
      tryDeleteOptions.close();
      columnHandlesBySegmentIdentifier.values().stream()
          .map(RocksDBSegmentIdentifier::get)
          .forEach(ColumnFamilyHandle::close);
      getDB().close();
    }
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  void throwIfClosed() {
    if (closed.get()) {
      LOG.error("Attempting to use a closed RocksDbKeyValueStorage");
      throw new IllegalStateException("Storage has been closed");
    }
  }

  record SegmentRecord(String name, byte[] id) {
    public String forDisplay() {
      return String.format("'%s'(%s)", name, Bytes.of(id).toHexString());
    }
  }

  @Override
  public Set<byte[]> getAllKeysThat(
      final Segment segment, final Predicate<byte[]> returnCondition) {
    return stream(segment)
        .filter(pair -> returnCondition.test(pair.getKey()))
        .map(Pair::getKey)
        .collect(toUnmodifiableSet());
  }
}
