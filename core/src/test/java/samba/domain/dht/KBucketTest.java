package samba.domain.dht;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static samba.domain.dht.BucketEntry.MIN_MILLIS_BETWEEN_PINGS;

import samba.StubClock;
import samba.TestHelper;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.util.Functions;
import org.junit.jupiter.api.Test;

class KBucketTest {

  private final StubClock clock = new StubClock();
  private final LivenessManager livenessManager =
      mock(LivenessManager.class); // new LivenessManager(clock, node ->
  // CompletableFuture.completedFuture(null));
  private static final KeyPair KEY_PAIR = Functions.randomKeyPair();
  private final KBucket bucket = new KBucket(livenessManager, clock);

  @Test
  void addOrUpdate_shouldHaveLiveNodesAndNotLivenessCheck() {
    final NodeRecord node1 = TestHelper.createNodeRecord();
    final NodeRecord node2 = TestHelper.createNodeRecord();
    final NodeRecord node3 = TestHelper.createNodeRecord();

    bucket.addOrUpdate(node1);
    bucket.addOrUpdate(node2);
    bucket.addOrUpdate(node3);

    assertThat(bucket.getAllNodes()).containsExactly(node3, node2, node1);
    assertFalse(bucket.getLiveNodes().isEmpty());

    verify(livenessManager, never()).checkLiveness(node1);
    verify(livenessManager, never()).checkLiveness(node2);
    verify(livenessManager, never()).checkLiveness(node3);
  }

  @Test
  void addOrUpdate_shouldHaveNodeInvertedOrderedAndOneLivenessCheckOnFirstNode() {
    final NodeRecord node1 = TestHelper.createNodeRecord();
    final NodeRecord node2 = TestHelper.createNodeRecord();

    bucket.addOrUpdate(node1);
    bucket.addOrUpdate(node2);

    clock.advanceTimeMillis(MIN_MILLIS_BETWEEN_PINGS);
    final NodeRecord node3 = TestHelper.createNodeRecord();
    bucket.addOrUpdate(node3);

    // Should trigger a new liveness check for the last item in the bucket
    verify(livenessManager, times(1)).checkLiveness(node1);
    assertThat(bucket.getAllNodes()).containsExactly(node3, node2, node1);
  }

  @Test
  void addOrUpdate_shouldReplaceExistingNode() {
    final NodeRecord node1 = TestHelper.createNodeRecord();
    final NodeRecord node1Updated =
        node1.withUpdatedCustomField("Hi", Bytes.fromHexString("0x0000"), KEY_PAIR.secretKey());

    bucket.addOrUpdate(node1);
    bucket.addOrUpdate(node1Updated);

    assertThat(bucket.getAllNodes()).containsExactly(node1Updated);
  }

  @Test
  void addOrUpdate_shouldNotUpdateExistingEntryWhenNewRecordIsOlder() {
    final NodeRecord node = TestHelper.createNodeRecord();
    final NodeRecord nodeUpdated =
        node.withUpdatedCustomField("Hi", Bytes.fromHexString("0x0000"), KEY_PAIR.secretKey());

    bucket.addOrUpdate(nodeUpdated);
    bucket.addOrUpdate(node);

    verify(livenessManager, never()).checkLiveness(nodeUpdated);
    verify(livenessManager, never()).checkLiveness(node);

    assertThat(bucket.getAllNodes()).containsExactly(nodeUpdated);
  }

  @Test
  void addOrUpdate_shouldNotUpdateExistingEntryWhenNewRecordIsSameAge() {
    final NodeRecord node = TestHelper.createNodeRecord();

    bucket.addOrUpdate(node);
    bucket.addOrUpdate(node);

    verify(livenessManager, never()).checkLiveness(node);
    assertThat(bucket.getAllNodes()).containsExactly(node);
  }

  @Test
  void addOrUpdate_shouldMoveUpdatedNodeToTheFrontOfTheBucket() {
    final NodeRecord node1 = TestHelper.createNodeRecord();
    final NodeRecord node2 = TestHelper.createNodeRecord();
    final NodeRecord node2Seq2 =
        node2.withUpdatedCustomField("hello", Bytes.fromHexString("0x1234"), KEY_PAIR.secretKey());

    bucket.addOrUpdate(node2);
    bucket.addOrUpdate(node1);

    assertThat(bucket.getAllNodes()).containsExactly(node1, node2);

    bucket.addOrUpdate(node2Seq2);
    assertThat(bucket.getAllNodes()).containsExactly(node2Seq2, node1);
  }

  @Test
  void removeNodeRecordFromTheBucket() {
    final NodeRecord node1 = TestHelper.createNodeRecord();
    final NodeRecord node2 = TestHelper.createNodeRecord();
    bucket.addOrUpdate(node2);
    bucket.addOrUpdate(node1);
    assertThat(bucket.getAllNodes()).containsExactly(node1, node2);
    bucket.remove(node1);
    assertThat(bucket.getAllNodes()).containsExactly(node2);
  }
}
