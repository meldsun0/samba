/*
 * SPDX-License-Identifier: Apache-2.0
 */

package samba.domain.dht;

import java.util.stream.IntStream;

import org.ethereum.beacon.discovery.storage.KBuckets;

public class BucketStats {
  private final int[] allNodeCounts = new int[KBuckets.MAXIMUM_BUCKET + 1];
  private final int[] liveNodeCounts = new int[KBuckets.MAXIMUM_BUCKET + 1];

  void setBucketStat(final int distance, final int liveNodeCount, final int allNodeCount) {
    liveNodeCounts[distance] = liveNodeCount;
    allNodeCounts[distance] = allNodeCount;
  }

  public int getTotalLiveNodeCount() {
    return IntStream.of(liveNodeCounts).sum();
  }

  public int getTotalNodeCount() {
    return IntStream.of(allNodeCounts).sum();
  }

  @Override
  public String toString() {
    final StringBuilder str = new StringBuilder("Node counts by distance:\n");
    for (int distance = 0; distance <= KBuckets.MAXIMUM_BUCKET; distance++) {
      final int liveCount = liveNodeCounts[distance];
      final int allCount = allNodeCounts[distance];
      if (allCount > 0 || liveCount > 0) {
        str.append("Distance: ")
            .append(distance)
            .append(" Live: ")
            .append(liveCount)
            .append(" Total: ")
            .append(allCount)
            .append("\n");
      }
    }
    str.append("Total Live: ")
        .append(getTotalLiveNodeCount())
        .append(" Total: ")
        .append(getTotalNodeCount());
    return str.toString();
  }
}
