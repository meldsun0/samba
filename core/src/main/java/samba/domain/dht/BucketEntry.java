package samba.domain.dht;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

// TODO evaluate if it should not be public.
public class BucketEntry {

  static final long MIN_MILLIS_BETWEEN_PINGS = TimeUnit.SECONDS.toMillis(30);
  static final long PING_TIMEOUT_MILLIS = 500;

  static final int NEVER = -1;
  private long lastPingTime = NEVER;
  private final long lastLivenessConfirmationTime;

  private final LivenessManager livenessManager;
  private final NodeRecord node;

  public BucketEntry(
      final LivenessManager livenessManager,
      final NodeRecord node,
      final long lastLivenessConfirmationTime) {
    this.livenessManager = livenessManager;
    this.node = node;
    this.lastLivenessConfirmationTime = lastLivenessConfirmationTime;
  }

  public Bytes getNodeId() {
    return node.getNodeId();
  }

  public NodeRecord getNode() {
    return node;
  }

  public void checkLiveness(final long currentTime) {
    if (currentTime - lastPingTime
            >= MIN_MILLIS_BETWEEN_PINGS // enough time has passed since the last "ping" to this node
        && currentTime - lastLivenessConfirmationTime
            >= MIN_MILLIS_BETWEEN_PINGS) { // enough time has passed since the last confirmation of
      // liveness.
      livenessManager.checkLiveness(node);
      lastPingTime = currentTime;
    }
  }

  public boolean hasFailedLivenessCheck(final long currentTime) {
    return lastLivenessConfirmationTime < lastPingTime // Haven't responded to last ping
        && currentTime - lastPingTime >= PING_TIMEOUT_MILLIS;
  }

  /**
   * A bucket entry is considered live if it has ever been confirmed as live. This avoids
   * considering the node dead just because we haven't pinged it recently or because it is yet to
   * respond to a ping.
   *
   * <p>Entries that fail to respond to a ping will eventually be removed from the k-buckets
   * entirely and disposed.
   *
   * @return true if the node has ever been confirmed as live.
   */
  public boolean isLive() {
    return lastLivenessConfirmationTime != NEVER;
  }

  public BucketEntry withLastConfirmedTime(final long currentTime) {
    return new BucketEntry(livenessManager, node, currentTime);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BucketEntry that = (BucketEntry) o;
    return node.getNodeId().equals(that.node.getNodeId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(node.getNodeId());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("node", node)
        .add("lastLivenessConfirmationTime", lastLivenessConfirmationTime)
        .add("lastPingTime", lastPingTime)
        .toString();
  }
}
