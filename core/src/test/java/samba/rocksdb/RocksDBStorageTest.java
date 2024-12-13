package samba.rocksdb;

import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import samba.storage.rocksdb.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tuweni.bytes.Bytes;
import org.assertj.core.api.Assertions;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test are based on AbstractKeyValueStorageTest from Besu */
public class RocksDBStorageTest {

  @TempDir public Path folder;

  /* private final ObservableMetricsSystem metricsSystemMock = mock(ObservableMetricsSystem.class);
  private final LabelledMetric<OperationTimer> labelledMetricOperationTimerMock =
      mock(LabelledMetric.class);
  private  final LabelledMetric<Counter> labelledMetricCounterMock = mock(LabelledMetric.class);
  private final OperationTimer operationTimerMock = mock(OperationTimer.class);*/

  @Test
  public void twoStoresAreIndependent() throws Exception {
    try (final KeyValueStorage store1 = createStore()) {
      try (final KeyValueStorage store2 = createStore()) {

        final KeyValueStorageTransaction tx = store1.startTransaction();
        final byte[] key = bytesFromHexString("0001");
        final byte[] value = bytesFromHexString("0FFF");

        tx.put(TestSegment.FOO, key, value);
        tx.commit();

        final Optional<byte[]> result = store2.get(TestSegment.FOO, key);
        assertThat(result).isEmpty();
      }
    }
  }

  @Test
  public void put() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final byte[] key = bytesFromHexString("0F");
      final byte[] firstValue = bytesFromHexString("0ABC");
      final byte[] secondValue = bytesFromHexString("0DEF");

      KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, key, firstValue);
      tx.commit();
      assertThat(store.get(TestSegment.FOO, key)).contains(firstValue);

      tx = store.startTransaction();
      tx.put(TestSegment.FOO, key, secondValue);
      tx.commit();
      assertThat(store.get(TestSegment.FOO, key)).contains(secondValue);
    }
  }

  @Test
  public void streamKeys() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final KeyValueStorageTransaction tx = store.startTransaction();
      final List<byte[]> keys =
          Stream.of("0F", "10", "11", "12")
              .map(this::bytesFromHexString)
              .collect(toUnmodifiableList());
      keys.forEach(key -> tx.put(TestSegment.FOO, key, bytesFromHexString("0ABC")));
      tx.commit();
      Assertions.assertThat(
              store.stream(TestSegment.FOO).map(Pair::getKey).collect(toUnmodifiableSet()))
          .containsExactlyInAnyOrder(keys.toArray(new byte[][] {}));
    }
  }

  @Test
  public void getAllKeysThat() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesFromHexString("0F"), bytesFromHexString("0ABC"));
      tx.put(TestSegment.FOO, bytesFromHexString("10"), bytesFromHexString("0ABC"));
      tx.put(TestSegment.FOO, bytesFromHexString("11"), bytesFromHexString("0ABC"));
      tx.put(TestSegment.FOO, bytesFromHexString("12"), bytesFromHexString("0ABC"));
      tx.commit();
      Set<byte[]> keys =
          store.getAllKeysThat(TestSegment.FOO, bv -> Bytes.wrap(bv).toString().contains("1"));
      Assertions.assertThat(keys.size()).isEqualTo(3);
      Assertions.assertThat(keys)
          .containsExactlyInAnyOrder(
              bytesFromHexString("10"), bytesFromHexString("11"), bytesFromHexString("12"));
    }
  }

  @Test
  public void containsKey() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final byte[] key = bytesFromHexString("ABCD");
      final byte[] value = bytesFromHexString("DEFF");

      assertThat(store.containsKey(TestSegment.FOO, key)).isFalse();

      final KeyValueStorageTransaction transaction = store.startTransaction();
      transaction.put(TestSegment.FOO, key, value);
      transaction.commit();

      assertThat(store.containsKey(TestSegment.FOO, key)).isTrue();
    }
  }

  @Test
  public void removeExisting() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final byte[] key = bytesFromHexString("0F");
      final byte[] value = bytesFromHexString("0ABC");

      KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, key, value);
      tx.commit();

      tx = store.startTransaction();
      tx.remove(TestSegment.FOO, key);
      tx.commit();
      assertThat(store.get(TestSegment.FOO, key)).isEmpty();
    }
  }

  @Test
  public void removeExistingSameTransaction() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final byte[] key = bytesFromHexString("0F");
      final byte[] value = bytesFromHexString("0ABC");

      KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, key, value);
      tx.remove(TestSegment.FOO, key);
      tx.commit();
      assertThat(store.get(TestSegment.FOO, key)).isEmpty();
    }
  }

  @Test
  public void removeNonExistent() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final byte[] key = bytesFromHexString("0F");
      KeyValueStorageTransaction tx = store.startTransaction();
      tx.remove(TestSegment.FOO, key);
      tx.commit();
      assertThat(store.get(TestSegment.FOO, key)).isEmpty();
    }
  }

  @Test
  public void concurrentUpdate() throws Exception {
    final int keyCount = 1000;
    try (final KeyValueStorage store = createStore()) {

      final CountDownLatch finishedLatch = new CountDownLatch(2);
      final Function<byte[], Thread> updater =
          (value) ->
              new Thread(
                  () -> {
                    try {
                      for (int i = 0; i < keyCount; i++) {
                        KeyValueStorageTransaction tx = store.startTransaction();
                        tx.put(TestSegment.FOO, Bytes.minimalBytes(i).toArrayUnsafe(), value);
                        tx.commit();
                      }
                    } finally {
                      finishedLatch.countDown();
                    }
                  });

      // Run 2 concurrent transactions that write a bunch of values to the same keys
      final byte[] a = Bytes.of(10).toArrayUnsafe();
      final byte[] b = Bytes.of(20).toArrayUnsafe();
      updater.apply(a).start();
      updater.apply(b).start();

      finishedLatch.await();

      for (int i = 0; i < keyCount; i++) {
        final byte[] key = Bytes.minimalBytes(i).toArrayUnsafe();
        final byte[] actual = store.get(TestSegment.FOO, key).get();
        assertThat(Arrays.equals(actual, a) || Arrays.equals(actual, b)).isTrue();
      }
    }
  }

  @Test
  public void transactionCommit() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      // Add some values
      KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(2));
      tx.put(TestSegment.FOO, bytesOf(3), bytesOf(3));
      tx.commit();

      // Start transaction that adds, modifies, and removes some values
      tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(3));
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(4));
      tx.remove(TestSegment.FOO, bytesOf(3));
      tx.put(TestSegment.FOO, bytesOf(4), bytesOf(8));

      // Check values before committing have not changed
      assertThat(store.get(TestSegment.FOO, bytesOf(1))).contains(bytesOf(1));
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).contains(bytesOf(2));
      assertThat(store.get(TestSegment.FOO, bytesOf(3))).contains(bytesOf(3));
      assertThat(store.get(TestSegment.FOO, bytesOf(4))).isEmpty();

      tx.commit();

      // Check that values have been updated after commit
      assertThat(store.get(TestSegment.FOO, bytesOf(1))).contains(bytesOf(1));
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).contains(bytesOf(4));
      assertThat(store.get(TestSegment.FOO, bytesOf(3))).isEmpty();
      assertThat(store.get(TestSegment.FOO, bytesOf(4))).contains(bytesOf(8));
    }
  }

  @Test
  public void transactionRollback() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      // Add some values
      KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(2));
      tx.put(TestSegment.FOO, bytesOf(3), bytesOf(3));
      tx.commit();

      // Start transaction that adds, modifies, and removes some values
      tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(3));
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(4));
      tx.remove(TestSegment.FOO, bytesOf(3));
      tx.put(TestSegment.FOO, bytesOf(4), bytesOf(8));

      // Check values before committing have not changed
      assertThat(store.get(TestSegment.FOO, bytesOf(1))).contains(bytesOf(1));
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).contains(bytesOf(2));
      assertThat(store.get(TestSegment.FOO, bytesOf(3))).contains(bytesOf(3));
      assertThat(store.get(TestSegment.FOO, bytesOf(4))).isEmpty();

      tx.rollback();

      // Check that values have not changed after rollback
      assertThat(store.get(TestSegment.FOO, bytesOf(1))).contains(bytesOf(1));
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).contains(bytesOf(2));
      assertThat(store.get(TestSegment.FOO, bytesOf(3))).contains(bytesOf(3));
      assertThat(store.get(TestSegment.FOO, bytesOf(4))).isEmpty();
    }
  }

  @Test
  public void transactionCommitEmpty() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final KeyValueStorageTransaction tx = store.startTransaction();
      tx.commit();
    }
  }

  @Test
  public void transactionRollbackEmpty() throws Exception {
    try (final KeyValueStorage store = createStore()) {
      final KeyValueStorageTransaction tx = store.startTransaction();
      tx.rollback();
    }
  }

  @Test
  public void transactionPutAfterCommit() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.commit();
                tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionRemoveAfterCommit() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.commit();
                tx.remove(TestSegment.FOO, bytesOf(1));
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionPutAfterRollback() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.rollback();
                tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionRemoveAfterRollback() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.rollback();
                tx.remove(TestSegment.FOO, bytesOf(1));
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionCommitAfterRollback() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.rollback();
                tx.commit();
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionCommitTwice() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.commit();
                tx.commit();
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionRollbackAfterCommit() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.commit();
                tx.rollback();
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void transactionRollbackTwice() {
    Assertions.assertThatThrownBy(
            () -> {
              try (final KeyValueStorage store = createStore()) {
                final KeyValueStorageTransaction tx = store.startTransaction();
                tx.rollback();
                tx.rollback();
              }
            })
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void twoTransactions() throws Exception {
    try (final KeyValueStorage store = createStore()) {

      final KeyValueStorageTransaction tx1 = store.startTransaction();
      final KeyValueStorageTransaction tx2 = store.startTransaction();

      tx1.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
      tx2.put(TestSegment.FOO, bytesOf(2), bytesOf(2));

      tx1.commit();
      tx2.commit();

      assertThat(store.get(TestSegment.FOO, bytesOf(1))).contains(bytesOf(1));
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).contains(bytesOf(2));
    }
  }

  @Test
  public void transactionIsolation() throws Exception {
    final int keyCount = 1000;
    final KeyValueStorage store = createStore();

    final CountDownLatch finishedLatch = new CountDownLatch(2);
    final Function<byte[], Thread> txRunner =
        (value) ->
            new Thread(
                () -> {
                  final KeyValueStorageTransaction tx = store.startTransaction();
                  for (int i = 0; i < keyCount; i++) {
                    tx.put(TestSegment.FOO, Bytes.minimalBytes(i).toArrayUnsafe(), value);
                  }
                  try {
                    tx.commit();
                  } finally {
                    finishedLatch.countDown();
                  }
                });

    // Run 2 concurrent transactions that write a bunch of values to the same keys
    final byte[] a = bytesOf(10);
    final byte[] b = bytesOf(20);
    txRunner.apply(a).start();
    txRunner.apply(b).start();

    finishedLatch.await();

    // Check that transaction results are isolated (not interleaved)
    final List<byte[]> finalValues = new ArrayList<>(keyCount);
    for (int i = 0; i < keyCount; i++) {
      final byte[] key = Bytes.minimalBytes(i).toArrayUnsafe();
      finalValues.add(store.get(TestSegment.FOO, key).get());
    }

    // Expecting the same value for all entries
    final byte[] expected = finalValues.get(0);
    for (final byte[] actual : finalValues) {
      assertThat(actual).containsExactly(expected);
    }

    assertThat(Arrays.equals(expected, a) || Arrays.equals(expected, b)).isTrue();

    store.close();
  }

  @Test
  public void twoSegmentsAreIndependent() throws Exception {
    final KeyValueStorage store = createStore();

    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(TestSegment.BAR, bytesFromHexString("0001"), bytesFromHexString("0FFF"));
    tx.commit();

    final Optional<byte[]> result = store.get(TestSegment.FOO, bytesFromHexString("0001"));

    assertThat(result).isEmpty();

    store.close();
  }

  @Test
  public void canRemoveThroughSegmentIteration() throws Exception {
    // we're looping this in order to catch intermittent failures when rocksdb objects are not close
    // properly
    for (int i = 0; i < 50; i++) {
      final KeyValueStorage store = createStore();

      final KeyValueStorageTransaction tx = store.startTransaction();
      tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
      tx.put(TestSegment.FOO, bytesOf(2), bytesOf(2));
      tx.put(TestSegment.FOO, bytesOf(3), bytesOf(3));
      tx.put(TestSegment.BAR, bytesOf(4), bytesOf(4));
      tx.put(TestSegment.BAR, bytesOf(5), bytesOf(5));
      tx.put(TestSegment.BAR, bytesOf(6), bytesOf(6));
      tx.commit();

      store.stream(TestSegment.FOO)
          .map(Pair::getKey)
          .forEach(
              key -> {
                if (!Arrays.equals(key, bytesOf(3))) store.tryDelete(TestSegment.FOO, key);
              });
      store.stream(TestSegment.BAR)
          .map(Pair::getKey)
          .forEach(
              key -> {
                if (!Arrays.equals(key, bytesOf(4))) store.tryDelete(TestSegment.BAR, key);
              });

      for (final var segment : Set.of(TestSegment.FOO, TestSegment.BAR)) {
        assertThat(store.stream(segment).count()).isEqualTo(1);
      }

      assertThat(store.get(TestSegment.FOO, bytesOf(1))).isEmpty();
      assertThat(store.get(TestSegment.FOO, bytesOf(2))).isEmpty();
      assertThat(store.get(TestSegment.FOO, bytesOf(3))).contains(bytesOf(3));

      assertThat(store.get(TestSegment.BAR, bytesOf(4))).contains(bytesOf(4));
      assertThat(store.get(TestSegment.BAR, bytesOf(5))).isEmpty();
      assertThat(store.get(TestSegment.BAR, bytesOf(6))).isEmpty();

      store.close();
    }
  }

  @Test
  public void canGetThroughSegmentIteration() throws Exception {
    final KeyValueStorage store = createStore();
    final KeyValueStorageTransaction tx = store.startTransaction();
    tx.put(TestSegment.FOO, bytesOf(1), bytesOf(1));
    tx.put(TestSegment.FOO, bytesOf(2), bytesOf(2));
    tx.put(TestSegment.FOO, bytesOf(3), bytesOf(3));
    tx.put(TestSegment.BAR, bytesOf(4), bytesOf(4));
    tx.put(TestSegment.BAR, bytesOf(5), bytesOf(5));
    tx.put(TestSegment.BAR, bytesOf(6), bytesOf(6));
    tx.commit();

    final Set<byte[]> gotFromFoo =
        store.getAllKeysThat(TestSegment.FOO, x -> Arrays.equals(x, bytesOf(3)));
    final Set<byte[]> gotFromBar =
        store.getAllKeysThat(
            TestSegment.BAR, x -> Arrays.equals(x, bytesOf(4)) || Arrays.equals(x, bytesOf(5)));
    final Set<byte[]> gotEmpty =
        store.getAllKeysThat(TestSegment.FOO, x -> Arrays.equals(x, bytesOf(0)));

    assertThat(gotFromFoo.size()).isEqualTo(1);
    assertThat(gotFromBar.size()).isEqualTo(2);
    assertThat(gotEmpty).isEmpty();

    assertThat(gotFromFoo).containsExactlyInAnyOrder(bytesOf(3));
    assertThat(gotFromBar).containsExactlyInAnyOrder(bytesOf(4), bytesOf(5));

    store.close();
  }

  @Test
  public void dbShouldIgnoreExperimentalSegmentsIfNotExisted(@TempDir final Path testPath)
      throws Exception {
    // Create new db should ignore experimental column family
    KeyValueStorage store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(
                TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR, TestSegment.EXPERIMENTAL),
            List.of(TestSegment.EXPERIMENTAL),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
    store.close();

    // new db will be backward compatible with db without knowledge of experimental column family
    store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR),
            List.of(),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);

    store.close();
  }

  @Test
  public void dbShouldNotIgnoreExperimentalSegmentsIfExisted(@TempDir final Path tempDir)
      throws Exception {
    final Path testPath = tempDir.resolve("testdb");
    KeyValueStorage store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(
                TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR, TestSegment.EXPERIMENTAL),
            List.of(),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);

    store.close();

    try {
      store =
          new RocksDBInstance(
              RocksDBConfiguration.createDefault(testPath),
              Arrays.asList(TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR),
              List.of(),
              new NoOpMetricsSystem(),
              RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
      fail("DB without knowledge of experimental column family should fail");
    } catch (StorageException e) {
      assertThat(e.getMessage()).contains("Unhandled column families");
    }

    // Even if the column family is marked as ignored, as long as it exists, it will not be ignored
    // and the db opens normally
    store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(
                TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR, TestSegment.EXPERIMENTAL),
            List.of(TestSegment.EXPERIMENTAL),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
    store.close();
  }

  @Test
  public void dbWillBeBackwardIncompatibleAfterExperimentalSegmentsAreAdded(
      @TempDir final Path testPath) throws Exception {
    // Create new db should ignore experimental column family
    KeyValueStorage store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(
                TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR, TestSegment.EXPERIMENTAL),
            List.of(TestSegment.EXPERIMENTAL),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
    store.close();

    store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR),
            List.of(),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
    store.close();

    store =
        new RocksDBInstance(
            RocksDBConfiguration.createDefault(testPath),
            Arrays.asList(
                TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR, TestSegment.EXPERIMENTAL),
            List.of(),
            new NoOpMetricsSystem(),
            RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
    store.close();

    // Now, the db will be backward incompatible with db without knowledge of experimental column
    // family
    try {
      new RocksDBInstance(
          RocksDBConfiguration.createDefault(testPath),
          Arrays.asList(TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR),
          List.of(),
          new NoOpMetricsSystem(),
          RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
      fail("DB without knowledge of experimental column family should fail");
    } catch (StorageException e) {
      assertThat(e.getMessage()).contains("Unhandled column families");
    }
  }

  /**
   * TODO need to check it @Test public void createStoreMustCreateMetrics() throws Exception { //
   * Prepare mocks
   * when(labelledMetricOperationTimerMock.labels(any())).thenReturn(operationTimerMock);
   * when(metricsSystemMock.createLabelledTimer(eq(SambaMetricCategory.STORAGE), anyString(),
   * anyString(), any())).thenReturn(labelledMetricOperationTimerMock);
   * when(metricsSystemMock.createLabelledCounter(eq(SambaMetricCategory.STORAGE), anyString(),
   * anyString(), any())).thenReturn(labelledMetricCounterMock);
   *
   * <p>// Prepare argument captors final ArgumentCaptor<String> labelledTimersMetricsNameArgs =
   * ArgumentCaptor.forClass(String.class); final ArgumentCaptor<String> labelledTimersHelpArgs =
   * ArgumentCaptor.forClass(String.class); final ArgumentCaptor<String>
   * labelledCountersMetricsNameArgs = ArgumentCaptor.forClass(String.class); final
   * ArgumentCaptor<String> labelledCountersHelpArgs = ArgumentCaptor.forClass(String.class); final
   * ArgumentCaptor<String> longGaugesMetricsNameArgs = ArgumentCaptor.forClass(String.class); final
   * ArgumentCaptor<String> longGaugesHelpArgs = ArgumentCaptor.forClass(String.class);
   *
   * <p>// Actual call try (final KeyValueStorage store = new RocksDBInstance(
   * RocksDBConfiguration.createDefault(folder), List.of(TestSegment.DEFAULT, TestSegment.FOO),
   * List.of(TestSegment.EXPERIMENTAL), metricsSystemMock,
   * RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS)) {
   *
   * <p>verify(metricsSystemMock, times(4)) .createLabelledTimer( eq(SambaMetricCategory.STORAGE),
   * labelledTimersMetricsNameArgs.capture(), labelledTimersHelpArgs.capture(), any());
   * assertThat(labelledTimersMetricsNameArgs.getAllValues()).containsExactly("read_latency_seconds",
   * "remove_latency_seconds", "write_latency_seconds", "commit_latency_seconds");
   * assertThat(labelledTimersHelpArgs.getAllValues()).containsExactly("Latency for read from
   * RocksDB.", "Latency of remove requests from RocksDB.", "Latency for write to RocksDB.",
   * "Latency for commits to RocksDB.");
   *
   * <p>verify(metricsSystemMock, times(2)) .createLongGauge( eq(SambaMetricCategory.STORAGE),
   * longGaugesMetricsNameArgs.capture(), longGaugesHelpArgs.capture(), any(LongSupplier.class));
   * assertThat(longGaugesMetricsNameArgs.getAllValues()).containsExactly("rocks_db_table_readers_memory_bytes",
   * "rocks_db_files_size_bytes");
   * assertThat(longGaugesHelpArgs.getAllValues()).containsExactly("Estimated memory used for
   * RocksDB index and filter blocks in bytes", "Estimated database size in bytes");
   *
   * <p>verify(metricsSystemMock) .createLabelledCounter( eq(SambaMetricCategory.STORAGE),
   * labelledCountersMetricsNameArgs.capture(), labelledCountersHelpArgs.capture(), any());
   * assertThat(labelledCountersMetricsNameArgs.getValue()).isEqualTo("rollback_count");
   * assertThat(labelledCountersHelpArgs.getValue()).isEqualTo("Number of RocksDB transactions
   * rolled back."); } }
   */
  private KeyValueStorage createStore() throws Exception {
    return new RocksDBInstance(
        RocksDBConfiguration.createDefault(getTempSubFolder(folder)),
        Arrays.asList(TestSegment.DEFAULT, TestSegment.FOO, TestSegment.BAR),
        List.of(),
        new NoOpMetricsSystem(),
        RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);
  }

  protected byte[] bytesFromHexString(final String hex) {
    return Bytes.fromHexString(hex).toArrayUnsafe();
  }

  protected Path getTempSubFolder(final Path folder) throws Exception {
    return java.nio.file.Files.createTempDirectory(folder, null);
  }

  protected byte[] bytesOf(final int... bytes) {
    return Bytes.of(bytes).toArrayUnsafe();
  }
}
