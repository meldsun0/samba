package samba.network;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.requests.Offer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalGossip {

  public static final int MAX_GOSSIP_COUNT = 4;
  private static final Logger LOG = LoggerFactory.getLogger(PortalGossip.class);
  private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

  public static void gossip(
      final Network network, final Set<NodeRecord> nodes, final Bytes key, final Bytes content) {
    checkArgument(network != null, "Network must not be null");
    // checkArgument(nodes != null && !nodes.isEmpty(), "Nodes must not be null or empty");
    checkArgument(key != null, "Key must not be null");
    checkArgument(content != null, "Content must not be null");

    final List<Bytes> keyList = List.of(key);
    final List<Bytes> contentList = List.of(content);
    final Offer offer = new Offer(keyList);

    nodes.forEach(
        node ->
            SafeFuture.runAsync(
                () -> {
                  try {
                    network.offer(node, contentList, offer);
                  } catch (Exception e) {
                    LOG.error("Failed to gossip to node {}: {}", node, e.getMessage(), e);
                  }
                },
                EXECUTOR));
  }
}
