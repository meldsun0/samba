package samba.network.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static samba.TestHelper.createNodeRecord;

import samba.domain.content.ContentKey;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.response.Accept;
import samba.network.RoutingTable;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;
import samba.util.DefaultContent;
import samba.util.Util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class OfferMessageTests {

  private HistoryNetwork historyNetwork;
  private HistoryDB historyDB;
  private Discv5Client discv5Client;
  private UTPManager utpManager;
  private NodeRecord nodeRecord;

  @BeforeEach
  public void setUp() throws IllegalAccessException {
    this.historyDB = mock(HistoryDB.class);
    this.discv5Client = mock(Discv5Client.class);
    this.utpManager = mock(UTPManager.class);
    this.nodeRecord = createNodeRecord();
    this.historyNetwork = new HistoryNetwork(discv5Client, historyDB, utpManager);
    when(this.discv5Client.getNodeId()).thenReturn(Optional.of(createNodeRecord().getNodeId()));
    mockRoutingTableFindNode(this.historyNetwork);
  }

  @Test
  public void invalidArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            this.historyNetwork
                .offer(null, List.of(Bytes.fromHexString("0x")), new Offer(List.of(Bytes.EMPTY)))
                .get());
    assertThrows(
        IllegalArgumentException.class,
        () -> this.historyNetwork.offer(nodeRecord, null, new Offer(List.of(Bytes.EMPTY))).get());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            this.historyNetwork.offer(nodeRecord, List.of(Bytes.fromHexString("0x")), null).get());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            this.historyNetwork
                .offer(
                    nodeRecord,
                    List.of(Bytes.fromHexString("0x")),
                    new Offer(List.of(Bytes.fromHexString("0x"), Bytes.fromHexString("0x"))))
                .get());
  }

  @Test
  public void emptyContentList() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          this.historyNetwork.offer(nodeRecord, List.of(), new Offer(List.of(Bytes.EMPTY))).get();
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          this.historyNetwork
              .offer(nodeRecord, List.of(Bytes.fromHexString("0x")), new Offer(List.of()))
              .get();
        });
  }

  @Test
  public void responseIsNotAAccepMessage() throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(
            CompletableFuture.completedFuture(new Offer(List.of(Bytes.EMPTY)).getSszBytes()));

    List<Bytes> content = List.of(Bytes.fromHexString("0x"));
    Offer offer = new Offer(List.of(Bytes.EMPTY));
    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, content, offer).get();
    assertTrue(contentKeysBitList.isEmpty());
  }

  @Test
  public void getAndEmptyBitListIfAcceptResponseHasAnEmptyBitlist()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(CompletableFuture.completedFuture(new Accept(555, Bytes.of()).getSszBytes()));

    List<Bytes> content = List.of(Bytes.fromHexString("0x"));
    Offer offer = new Offer(List.of(Bytes.EMPTY));
    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, content, offer).get();

    assertTrue(contentKeysBitList.get().isEmpty());
    verify(historyDB, never()).get(any(ContentKey.class));
    verify(utpManager, never())
        .offerWrite(any(NodeRecord.class), any(Integer.class), any(Bytes.class));
  }

  @Test
  public void getAndEmptyBitListIfSendDiscv5MessageFail()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(SafeFuture.failedFuture(new Throwable()));

    List<Bytes> content = List.of(Bytes.fromHexString("0x"));
    Offer offer = new Offer(List.of(Bytes.EMPTY));
    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, content, offer).get();

    assertTrue(contentKeysBitList.isEmpty());
    verify(historyDB, never()).get(any(ContentKey.class));
    verify(utpManager, never())
        .offerWrite(any(NodeRecord.class), any(Integer.class), any(Bytes.class));
  }

  @Test
  public void getBitListOfAll0() throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createAcceptResponse(555, Bytes.of(new byte[] {0, 0, 0, 0})));

    List<Bytes> content = List.of(Bytes.fromHexString("0x"));
    Offer offer = new Offer(List.of(Bytes.EMPTY));
    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, content, offer).get();

    assertEquals(contentKeysBitList.get(), Bytes.of(new byte[] {0, 0, 0, 0}));
    verify(historyDB, never()).get(any(ContentKey.class));
    verify(utpManager, never())
        .offerWrite(any(NodeRecord.class), any(Integer.class), any(Bytes.class));
  }

  @Test
  public void getEmptyResponseIfFailsWhenContentIsBeingConcatenated()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createAcceptResponse(555, Bytes.of(new byte[] {1, 1, 1})));

    when(historyDB.get(any(ContentKey.class))).thenThrow(NullPointerException.class);

    List<Bytes> contentKey = List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3);
    List<Bytes> content =
        List.of(DefaultContent.value1, DefaultContent.value2, DefaultContent.value3);

    Offer offer = new Offer(contentKey);
    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, content, offer).get();

    assertTrue(contentKeysBitList.isEmpty());
    verify(utpManager, never())
        .offerWrite(any(NodeRecord.class), any(Integer.class), any(Bytes.class));
  }

  @Test
  public void sendOkOfferMessageWithEmptyContentAndGetAcceptedMessageAndAnOkResponse()
      throws ExecutionException, InterruptedException {
    when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(createAcceptResponse(555, Bytes.of(1)));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.of(Bytes.EMPTY));

    Offer offer = new Offer(List.of(DefaultContent.key3));

    Optional<Bytes> contentKeysBitList =
        this.historyNetwork.offer(nodeRecord, List.of(DefaultContent.value3), offer).get();

    verify(utpManager, times(1))
        .offerWrite(
            any(NodeRecord.class),
            eq(555),
            eq(
                Bytes.concatenate(
                    Util.writeUnsignedLeb128(DefaultContent.value3.size()),
                    DefaultContent.value3)));
    assertEquals(contentKeysBitList.get().toHexString(), Bytes.of(1).toHexString());
  }

  private static CompletableFuture<Bytes> createAcceptResponse(
      int connectionId, Bytes contentKeys) {
    return CompletableFuture.completedFuture(new Accept(connectionId, contentKeys).getSszBytes());
  }

  private void mockRoutingTableFindNode(HistoryNetwork historyNetwork)
      throws IllegalAccessException {
    RoutingTable mockedRoutingTable = mock(RoutingTable.class);
    Field field =
        ReflectionUtils.findFields(
                HistoryNetwork.class,
                f -> f.getName().equals("routingTable"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);
    field.setAccessible(true);
    field.set(historyNetwork, mockedRoutingTable);

    when(mockedRoutingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(createNodeRecord()));
  }
}
