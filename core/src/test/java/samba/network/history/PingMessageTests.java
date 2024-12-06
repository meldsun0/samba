package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static samba.TestHelper.createNodeRecord;

import samba.TestHelper;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.services.discovery.Discv5Client;
import samba.storage.HistoryDB;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PingMessageTests {

  private static final Bytes pongCustomPayload =
      Bytes.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f");
  private static final Bytes pingCustomPayload =
      Bytes.fromHexString("0xfeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

  @Test
  public void sendOkPingMessageAndReceiveOkPongTest()
      throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(pongCustomPayload));
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());

    HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));

    NodeRecord nodeRecord = createNodeRecord();
    Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

    assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());
    assertEquals(pongCustomPayload, pong.get().getCustomPayload());
    assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
    assertTrue(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(pong.get().getCustomPayload(), historyNetwork.getRadiusFromNode(nodeRecord));
  }

  @Test
  public void sendOkPingMessageAndReceiveOkEmptyCustomPayloadPongTest()
      throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(Bytes.EMPTY));
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());

    HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
    NodeRecord nodeRecord = createNodeRecord();
    Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

    assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());
    assertEquals(Bytes.EMPTY, pong.get().getCustomPayload());
    assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
    assertFalse(historyNetwork.isNodeConnected(nodeRecord));
    assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
  }

  @Test
  public void handleErrorWhenSendingaPingTest() throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> SafeFuture.failedFuture(new NullPointerException()));

    HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
    NodeRecord nodeRecord = createNodeRecord();

    Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

    assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
    assertFalse(historyNetwork.isNodeConnected(nodeRecord));
    assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
    assertFalse(pong.isPresent());
  }

  @Test
  public void handleASuccessfulPingIncomingRequestTest()
      throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(pongCustomPayload));
    when(discv5Client.getEnrSeq())
        .thenAnswer(invocation -> org.apache.tuweni.units.bigints.UInt64.valueOf(1));

    HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
    NodeRecord nodeRecord = TestHelper.createNodeRecord();
    Ping pingMessage = createPingMessage();

    historyNetwork.handlePing(nodeRecord, createPingMessage());

    assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
    assertTrue(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(pingMessage.getCustomPayload(), historyNetwork.getRadiusFromNode(nodeRecord));
  }

  @NotNull
  private static CompletableFuture<Bytes> createPongBytesResponse(Bytes pongCustomPayload) {
    return CompletableFuture.completedFuture(
        Bytes.concatenate(Bytes.fromHexString("0x0101000000000000000c000000"), pongCustomPayload));
  }

  @NotNull
  private static Ping createPingMessage() {
    return new Ping(UInt64.valueOf(1), pingCustomPayload);
  }
}
