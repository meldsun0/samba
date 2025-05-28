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
import samba.domain.messages.extensions.standard.ClientInfoAndCapabilities;
import samba.domain.messages.extensions.standard.ErrorExtension;
import samba.domain.messages.extensions.standard.ErrorType;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PingMessageTests {

  private static final Bytes payloadClientInfo =
      new ClientInfoAndCapabilities(
              "clientInfo",
              UInt256.valueOf(12),
              List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE))
          .getSszBytes();
  private static final Bytes payloadFailedToDecode =
      new ErrorExtension(ErrorType.FAILED_TO_DECODE.getErrorCode()).getSszBytes();
  private static final Bytes payloadExtensionNotSupported =
      new ErrorExtension(ErrorType.EXTENSION_NOT_SUPPORTED.getErrorCode()).getSszBytes();
  private HistoryDB historyDB;

  @BeforeEach
  public void setUp() {
    this.historyDB = mock(HistoryDB.class);
    when(historyDB.isAvailable()).thenReturn(true);
  }

  @Test
  public void
      sendOkClientInfoAndCapabilitiesPingMessageAndReceiveOkClientInfoAndCapabilitiesPongTest()
          throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.sendDiscv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(UInt16.ZERO, payloadClientInfo));
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());

    HistoryNetwork historyNetwork =
        new HistoryNetwork(
            discv5Client, this.historyDB, mock(UTPManager.class), mock(MetricsSystem.class));

    NodeRecord nodeRecord = createNodeRecord();
    Optional<Pong> pong =
        historyNetwork.ping(nodeRecord, createPingMessage(UInt16.ZERO, payloadClientInfo)).get();

    ClientInfoAndCapabilities extension =
        ClientInfoAndCapabilities.fromSszBytes(pong.get().getPayload());
    assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());
    assertEquals(payloadClientInfo, pong.get().getPayload());
    assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
    assertTrue(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(extension.getDataRadius(), historyNetwork.getRadiusFromNode(nodeRecord));
  }

  @Test
  public void handleErrorWhenSendingaPingTest() throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDiscv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> SafeFuture.failedFuture(new NullPointerException()));

    HistoryNetwork historyNetwork =
        new HistoryNetwork(
            discv5Client, this.historyDB, mock(UTPManager.class), mock(MetricsSystem.class));
    NodeRecord nodeRecord = createNodeRecord();

    Optional<Pong> pong =
        historyNetwork.ping(nodeRecord, createPingMessage(UInt16.ZERO, payloadClientInfo)).get();

    assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
    assertFalse(historyNetwork.isNodeConnected(nodeRecord));
    assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
    assertFalse(pong.isPresent());
  }

  @Test
  public void handleASuccessfulClientInfoAndCapabilitiesPingIncomingRequestTest()
      throws ExecutionException, InterruptedException {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDiscv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(UInt16.ZERO, payloadClientInfo));
    when(discv5Client.getEnrSeq())
        .thenAnswer(invocation -> org.apache.tuweni.units.bigints.UInt64.valueOf(1));

    HistoryNetwork historyNetwork =
        new HistoryNetwork(
            discv5Client, this.historyDB, mock(UTPManager.class), mock(MetricsSystem.class));
    NodeRecord nodeRecord = TestHelper.createNodeRecord();
    Ping pingMessage = createPingMessage(UInt16.ZERO, payloadClientInfo);

    historyNetwork.handlePing(nodeRecord, createPingMessage(UInt16.ZERO, payloadClientInfo));

    ClientInfoAndCapabilities extension =
        ClientInfoAndCapabilities.fromSszBytes(pingMessage.getPayload());
    assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
    assertTrue(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(extension.getDataRadius(), historyNetwork.getRadiusFromNode(nodeRecord));
  }

  @Test
  public void handleABadClientInfoAndCapabilitiesPingIncomingRequestTest() {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDiscv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(UInt16.ZERO, Bytes.EMPTY));
    when(discv5Client.getEnrSeq())
        .thenAnswer(invocation -> org.apache.tuweni.units.bigints.UInt64.valueOf(1));

    HistoryNetwork historyNetwork =
        new HistoryNetwork(
            discv5Client, this.historyDB, mock(UTPManager.class), mock(MetricsSystem.class));
    NodeRecord nodeRecord = TestHelper.createNodeRecord();

    Pong pong =
        (Pong) historyNetwork.handlePing(nodeRecord, createPingMessage(UInt16.ZERO, Bytes.EMPTY));

    assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
    assertFalse(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(payloadFailedToDecode, pong.getPayload());
  }

  @Test
  public void handleASuccessfulUnsupportedPingIncomingRequestTest() {
    Discv5Client discv5Client = mock(Discv5Client.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
    when(discv5Client.sendDiscv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> createPongBytesResponse(UInt16.ZERO, Bytes.EMPTY));
    when(discv5Client.getEnrSeq())
        .thenAnswer(invocation -> org.apache.tuweni.units.bigints.UInt64.valueOf(1));

    HistoryNetwork historyNetwork =
        new HistoryNetwork(
            discv5Client, this.historyDB, mock(UTPManager.class), mock(MetricsSystem.class));
    NodeRecord nodeRecord = TestHelper.createNodeRecord();

    Pong pong =
        (Pong)
            historyNetwork.handlePing(
                nodeRecord, createPingMessage(UInt16.valueOf(1234), Bytes.EMPTY));

    assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
    assertFalse(historyNetwork.isNodeConnected(nodeRecord));
    assertEquals(payloadExtensionNotSupported, pong.getPayload());
  }

  @NotNull
  private static CompletableFuture<Bytes> createPongBytesResponse(
      UInt16 extensionType, Bytes pongPayload) {
    return CompletableFuture.completedFuture(
        new Pong(UInt64.valueOf(1), extensionType, pongPayload).getSszBytes());
  }

  @NotNull
  private static Ping createPingMessage(UInt16 extensionType, Bytes pingPayload) {
    return new Ping(UInt64.valueOf(1), extensionType, pingPayload);
  }
}
