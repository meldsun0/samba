package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static samba.TestHelper.createNodeRecord;

import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.response.Content;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.results.FindContentResult;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class FindContentMessageTests {

  private static final Bytes contentKey =
      Bytes.fromHexString("0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c");
  private static final Bytes data = Bytes.fromHexString("0x1234567890");
  private static final int connectionId = 1;
  private HistoryDB historyDB;
  private Discv5Client discv5Client;
  private UTPManager utpManager;
  private NodeRecord nodeRecord;
  private HistoryNetwork historyNetwork;

  @BeforeEach
  public void setUp() {
    this.historyDB = mock(HistoryDB.class);
    this.discv5Client = mock(Discv5Client.class);
    this.utpManager = mock(UTPManager.class);
    this.nodeRecord = createNodeRecord();
    this.historyNetwork = new HistoryNetwork(discv5Client, historyDB, utpManager);
    when(historyDB.isAvailable()).thenReturn(true);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveOkContentConnectionIdTest()
      throws ExecutionException, InterruptedException {
    when(utpManager.findContentRead(any(NodeRecord.class), eq(connectionId)))
        .thenReturn(SafeFuture.completedFuture(Bytes.EMPTY));
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createContentConnectionIdBytesResponse(connectionId));

    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

    assertEquals(content.get().getContent(), "0x");
    assertEquals(content.get().getUtpTransfer(), true);
    assertEquals(content.get().getEnrs(), null);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveOkContentContentTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createContentContentBytesResponse(data));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

    assertEquals(content.get().getContent(), data.toHexString());
    assertEquals(content.get().getUtpTransfer(), false);
    assertEquals(content.get().getEnrs(), null);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveOkContentEnrTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createContentEnrBytesResponse(List.of("-LI=", "-LI=", "-LI=")));
    when(discv5Client.getEnr()).thenReturn(Optional.of("enr"));

    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

    assertEquals(content.get().getContent(), null);
    assertEquals(content.get().getUtpTransfer(), null);
    assertEquals(content.get().getEnrs().size(), 3);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveEmptyContentConnectionIdTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0500")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(Optional.empty(), content);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveEmptyContentContentTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0501")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals("0x", content.get().getContent());
  }

  @Test
  public void sendOkFindContentMessageAndRecieveEmptyContentEnrTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0502")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(List.of(), content.get().getEnrs());
  }

  @Test
  public void sendOkFindContentMessageAndRecieveBadContentPacketTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x05FFFFFFFF")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(Optional.empty(), content);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveBadContentConnectionIdTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0500FFFFFFFF")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(Optional.empty(), content);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveBadContentContentTest()
      throws ExecutionException, InterruptedException {
    byte[] largeContent = new byte[PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES + 1];
    for (int i = 0; i < PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES + 1; i++) {
      largeContent[i] = (byte) 0xFF;
    }
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.wrap(largeContent)));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(Optional.empty(), content);
  }

  @Test
  public void sendOkFindContentMessageAndRecieveBadContentEnrTest()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0502FFFFFFFF")));
    Optional<FindContentResult> content =
        historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();
    assertEquals(Optional.empty(), content);
  }

  private static CompletableFuture<Bytes> createContentConnectionIdBytesResponse(int connectionId) {
    return CompletableFuture.completedFuture(new Content(connectionId).getSszBytes());
  }

  private static CompletableFuture<Bytes> createContentContentBytesResponse(Bytes content) {
    return CompletableFuture.completedFuture(new Content(content).getSszBytes());
  }

  private static CompletableFuture<Bytes> createContentEnrBytesResponse(List<String> enrs) {
    return CompletableFuture.completedFuture(new Content(enrs).getSszBytes());
  }

  private static CompletableFuture<Bytes> createBadContentBytesResponse(Bytes badContent) {
    return CompletableFuture.completedFuture(badContent);
  }

  private static FindContent createFindContentMessage(Bytes contentKey) {
    return new FindContent(contentKey);
  }
}
