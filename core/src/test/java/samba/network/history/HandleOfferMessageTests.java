package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static samba.TestHelper.createNodeRecord;

import samba.domain.content.ContentKey;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.response.Accept;
import samba.network.history.api.HistoryNetworkProtocolMessageHandler;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;
import samba.util.DefaultContent;
import samba.util.ProtocolVersionUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandleOfferMessageTests {

  private HistoryNetworkProtocolMessageHandler historyNetwork;
  private HistoryDB historyDB;
  private Discv5Client discv5Client;
  private UTPManager utpManager;
  private NodeRecord nodeRecord;

  @BeforeEach
  public void setUp() {
    this.historyDB = mock(HistoryDB.class);
    this.discv5Client = mock(Discv5Client.class);
    this.utpManager = mock(UTPManager.class);
    this.nodeRecord = createNodeRecord();
    this.historyNetwork = new HistoryNetwork(discv5Client, historyDB, utpManager);

    when(this.discv5Client.getNodeId()).thenReturn(Optional.of(createNodeRecord().getNodeId()));
  }

  @Test
  public void respondEmptyBitlistOnAcceptMessageIfOfferMessageHasEmptyContentKeys() {
    Offer offer = new Offer(List.of());
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.EMPTY);
    verify(utpManager, never()).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageWithAll0BitListIfContentIsStoredLocallyProtocolV0() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.of(Bytes.EMPTY)); // Has content
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(0, 0, 0));
    verify(utpManager, never()).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageWithAll2ByteListIfContentIsStoredLocallyProtocolV1() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.of(Bytes.EMPTY)); // Has content
    when(utpManager.acceptRead(any(NodeRecord.class), any(Consumer.class))).thenReturn(555);
    ProtocolVersionUtil.setSupportedProtocolVersions(nodeRecord, List.of(1));
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(2, 2, 2));
    verify(utpManager, never()).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageWithAll1BitListIfContentIsNotStoredLocallyProtocolV0() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.empty()); // No content on DB
    when(utpManager.acceptRead(any(NodeRecord.class), any(Consumer.class))).thenReturn(555);
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(1, 1, 1));
    assertEquals(accept.getConnectionId(), 555);
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageWithAll0ByteListIfContentIsNotStoredLocallyProtocolV1() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.empty()); // No content on DB
    when(utpManager.acceptRead(any(NodeRecord.class), any(Consumer.class))).thenReturn(555);
    ProtocolVersionUtil.setSupportedProtocolVersions(nodeRecord, List.of(1));
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(0, 0, 0));
    assertEquals(accept.getConnectionId(), 555);
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageProtocolV0() {
    Offer offer = new Offer(List.of(DefaultContent.key3));
    when(historyDB.get(ContentKey.decode(DefaultContent.key3)))
        .thenReturn(Optional.of(DefaultContent.value3));
    when(utpManager.acceptRead(any(NodeRecord.class), any(Consumer.class))).thenReturn(555);

    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(Bytes.of(1), accept.getContentKeys());
    assertEquals(555, accept.getConnectionId());
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageProtocolV1() {
    Offer offer = new Offer(List.of(DefaultContent.key3));
    when(historyDB.get(ContentKey.decode(DefaultContent.key3)))
        .thenReturn(Optional.of(DefaultContent.value3));
    when(utpManager.acceptRead(any(NodeRecord.class), any(Consumer.class))).thenReturn(555);
    ProtocolVersionUtil.setSupportedProtocolVersions(nodeRecord, List.of(1));
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(Bytes.of(0), accept.getContentKeys());
    assertEquals(555, accept.getConnectionId());
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), any(Consumer.class));
  }

  @Test
  public void responseAcceptMessageEmptyIfThereIsAnException() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenThrow(NullPointerException.class);
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.EMPTY);
  }
}
