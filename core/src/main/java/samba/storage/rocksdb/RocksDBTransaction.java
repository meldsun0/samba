package samba.storage.rocksdb;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Function;
import java.util.function.Supplier;

import org.hyperledger.besu.plugin.services.metrics.OperationTimer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBTransaction implements KeyValueStorageTransaction {
  private static final Logger logger = LoggerFactory.getLogger(RocksDBTransaction.class);
  private static final String NO_SPACE_LEFT_ON_DEVICE = "No space left on device";

  private final RocksDBMetrics metrics;
  private final Transaction innerTx;
  private final WriteOptions options;
  private final Function<Segment, ColumnFamilyHandle> columnFamilyMapper;
  private final Supplier<Boolean> isClosed;
  private boolean active = true;

  /**
   * Instantiates a new RocksDb transaction.
   *
   * @param columnFamilyMapper mapper from segment identifier to column family handle
   * @param innerTx the inner tx
   * @param options the options
   * @param metrics the metrics
   */
  public RocksDBTransaction(
      final Function<Segment, ColumnFamilyHandle> columnFamilyMapper,
      final Transaction innerTx,
      final WriteOptions options,
      final RocksDBMetrics metrics,
      final Supplier<Boolean> isClosed) {
    this.columnFamilyMapper = columnFamilyMapper;
    this.innerTx = innerTx;
    this.options = options;
    this.metrics = metrics;
    this.isClosed = isClosed;
  }

  @Override
  public void put(final Segment segmentId, final byte[] key, final byte[] value) {
    checkState(active, "Cannot invoke put() on a completed transaction.");
    checkState(!isClosed.get(), "Cannot invoke put() on a closed storage.");

    try (final OperationTimer.TimingContext ignored = metrics.writeLatency().startTimer()) {
      innerTx.put(columnFamilyMapper.apply(segmentId), key, value);
    } catch (final RocksDBException e) {
      if (e.getMessage().contains(NO_SPACE_LEFT_ON_DEVICE)) {
        logger.error(e.getMessage());
        System.exit(0);
      }
      throw new StorageException(e);
    }
  }

  @Override
  public void remove(final Segment segmentId, final byte[] key) {
    checkState(active, "Cannot invoke remove() on a completed transaction.");
    checkState(!isClosed.get(), "Cannot invoke remove() on a closed storage.");

    try (final OperationTimer.TimingContext ignored = metrics.removeLatency().startTimer()) {
      innerTx.delete(columnFamilyMapper.apply(segmentId), key);
    } catch (final RocksDBException e) {
      if (e.getMessage().contains(NO_SPACE_LEFT_ON_DEVICE)) {
        logger.error(e.getMessage());
        System.exit(0);
      }
      throw new StorageException(e);
    }
  }

  @Override
  public void commit() throws StorageException {
    checkState(active, "Cannot commit a completed transaction.");
    checkState(!isClosed.get(), "Cannot invoke commit() on a closed storage.");
    active = false;
    try (final OperationTimer.TimingContext ignored = metrics.commitLatency().startTimer()) {
      innerTx.commit();
    } catch (final RocksDBException e) {
      if (e.getMessage().contains(NO_SPACE_LEFT_ON_DEVICE)) {
        logger.error(e.getMessage());
        System.exit(0);
      }
      throw new StorageException(e);
    } finally {
      close();
    }
  }

  @Override
  public void rollback() {
    checkState(active, "Cannot rollback a completed transaction.");
    checkState(!isClosed.get(), "Cannot invoke rollback() on a closed storage.");
    active = false;
    try {
      innerTx.rollback();
      metrics.rollbackCount().inc();
    } catch (final RocksDBException e) {
      if (e.getMessage().contains(NO_SPACE_LEFT_ON_DEVICE)) {
        logger.error(e.getMessage());
        System.exit(0);
      }
      throw new StorageException(e);
    } finally {
      close();
    }
  }

  private void close() {
    innerTx.close();
    options.close();
  }
}
