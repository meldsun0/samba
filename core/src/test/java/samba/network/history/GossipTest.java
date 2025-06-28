package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import samba.TestHelper;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;
import samba.util.DefaultContent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class GossipTest {

  private Set<NodeRecord> nodes;

  @BeforeEach
  public void setUp() {
    this.nodes = new HashSet<>();
    for (int i = 0; i < 5; i++) {
      nodes.add(TestHelper.createNodeRecord());
    }
  }

  @Test
  public void testGossipEmptyKey() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(5);

    HistoryNetwork historyNetwork1 =
        new HistoryNetwork(
            mock(Discv5Client.class),
            mock(HistoryDB.class),
            mock(UTPManager.class),
            mock(MetricsSystem.class));
    HistoryNetwork historyNetworkWithMocks1 = spy(historyNetwork1);
    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(historyNetworkWithMocks1)
        .offer(any(), any(), any());

    historyNetworkWithMocks1.gossip(nodes, Bytes.EMPTY, Bytes.EMPTY);
    assertTrue(latch.await(1, TimeUnit.SECONDS), "Expected all gossip tasks to complete");
    verify(historyNetworkWithMocks1, times(5)).offer(any(), any(), any());
  }

  @Test
  public void testGossipValid() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(5);

    HistoryNetwork historyNetwork2 =
        new HistoryNetwork(
            mock(Discv5Client.class),
            mock(HistoryDB.class),
            mock(UTPManager.class),
            mock(MetricsSystem.class));

    HistoryNetwork historyNetworkWithMocks2 = spy(historyNetwork2);

    Bytes key = DefaultContent.key1;
    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(historyNetworkWithMocks2)
        .offer(any(), any(), any());

    historyNetworkWithMocks2.gossip(nodes, key, Bytes.EMPTY);
    assertTrue(latch.await(1, TimeUnit.SECONDS), "Expected all gossip tasks to complete");
    verify(historyNetworkWithMocks2, times(5)).offer(any(), any(), any());
  }

  @Test
  public void testGossipInvalid() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(5);

    HistoryNetwork historyNetwork3 =
        new HistoryNetwork(
            mock(Discv5Client.class),
            mock(HistoryDB.class),
            mock(UTPManager.class),
            mock(MetricsSystem.class));

    HistoryNetwork historyNetworkWithMocks3 = spy(historyNetwork3);

    Bytes key = DefaultContent.key1;
    doAnswer(
            invocation -> {
              latch.countDown();
              return SafeFuture.failedFuture(new RuntimeException("Simulated async failure"));
            })
        .when(historyNetworkWithMocks3)
        .offer(any(), any(), any());

    historyNetworkWithMocks3.gossip(nodes, key, Bytes.EMPTY);
    assertTrue(latch.await(1, TimeUnit.SECONDS), "Expected all gossip tasks to complete");
    verify(historyNetworkWithMocks3, times(5)).offer(any(), any(), any());
  }
}
