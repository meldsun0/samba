package samba.network;

import samba.domain.messages.requests.Offer;

import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalGossip {
  public static final int MAX_GOSSIP_COUNT = 4;

  public static void gossip(
      final Network network, final Set<NodeRecord> nodes, final Bytes key, final Bytes content) {
    List<Bytes> keyList = List.of(key);
    List<Bytes> contentList = List.of(content);
    SafeFuture.runAsync(
        () -> nodes.forEach(node -> network.offer(node, contentList, new Offer(keyList))));
  }
}
