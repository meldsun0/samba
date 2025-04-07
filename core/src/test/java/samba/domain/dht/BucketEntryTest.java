package samba.domain.dht;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static samba.domain.dht.BucketEntry.MIN_MILLIS_BETWEEN_PINGS;
import static samba.domain.dht.BucketEntry.NEVER;
import static samba.domain.dht.BucketEntry.PING_TIMEOUT_MILLIS;

import samba.TestHelper;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BucketEntryTest {

  private static final int START_TIME = 1_000_000;
  private final NodeRecord nodeRecord = TestHelper.createNodeRecord();
  private final LivenessManager livenessManager = Mockito.mock(LivenessManager.class);
  private BucketEntry entry = new BucketEntry(livenessManager, nodeRecord, NEVER);

  @Test
  void checkLiveness_shouldCheckLivenessWhenNeverPinged() {
    entry.checkLiveness(START_TIME);
    verify(livenessManager).checkLiveness(nodeRecord);
  }

  @Test
  void checkLiveness_shouldNotCheckLivenessWhenPingSentRecently() {
    entry.checkLiveness(START_TIME);
    verify(livenessManager).checkLiveness(nodeRecord);

    entry.checkLiveness(START_TIME + MIN_MILLIS_BETWEEN_PINGS - 1);
    verifyNoMoreInteractions(livenessManager);
  }

  @Test
  void checkLiveness_shouldNotCheckLivenessWhenLivenessConfirmedRecently() {
    createEntryWithLivenessConfirmationTime(START_TIME);

    entry.checkLiveness(START_TIME + 1);
    verifyNoMoreInteractions(livenessManager);
  }

  @Test
  void hasFailedLivenessCheck_shouldBeFalseWhenNoLivenessCheckPerformed() {
    assertThat(entry.hasFailedLivenessCheck(10000000)).isFalse();
  }

  @Test
  void hasFailedLivenessCheck_shouldBeTrueWhenLastPingNotRespondedToInTime() {
    entry.checkLiveness(START_TIME);

    assertThat(entry.hasFailedLivenessCheck(START_TIME + PING_TIMEOUT_MILLIS)).isTrue();
  }

  @Test
  void hasFailedLivenessCheck_shouldBeFalseWhenLastPingWithinTimeout() {
    entry.checkLiveness(START_TIME);

    assertThat(entry.hasFailedLivenessCheck(START_TIME + PING_TIMEOUT_MILLIS - 1)).isFalse();
  }

  @Test
  void isLive_shouldBeLiveIfConfirmed() {
    createEntryWithLivenessConfirmationTime(START_TIME + 4000);
    assertThat(entry.isLive()).isTrue();
  }

  @Test
  void isLive_shouldBeLiveIfConfirmedPriorToLastPing() {
    createEntryWithLivenessConfirmationTime(START_TIME + 4000);
    entry.checkLiveness(START_TIME + MIN_MILLIS_BETWEEN_PINGS + 1);
    assertThat(entry.isLive()).isTrue();
  }

  @Test
  void isLive_shouldNotBeLiveWhenNotConfirmed() {
    assertThat(entry.isLive()).isFalse();
  }

  private void createEntryWithLivenessConfirmationTime(final int lastLivenessConfirmationTime) {
    entry = new BucketEntry(livenessManager, nodeRecord, lastLivenessConfirmationTime);
  }
}
