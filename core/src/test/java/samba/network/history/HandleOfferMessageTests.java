package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static samba.TestHelper.createNodeRecord;

import samba.domain.content.ContentKey;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.response.Accept;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;
import samba.storage.HistoryDB;
import samba.util.DefaultContent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandleOfferMessageTests {

  private HistoryNetworkIncomingRequests historyNetwork;
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
  }

  @Test
  public void respondEmptyBitlistOnAcceptMessageIfOfferMessageHasEmptyContentKeys() {
    Offer offer = new Offer(List.of());
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.EMPTY);
    verify(utpManager, never())
        .acceptRead(any(NodeRecord.class), any(Integer.class), any(Consumer.class));
    verify(utpManager, never()).generateConnectionId();
  }

  @Test
  public void responseAcceptMessageWithAll0BitListIfContentIsStoredLocally() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.of(Bytes.EMPTY)); // Has content
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(0, 0, 0));
    verify(utpManager, never())
        .acceptRead(any(NodeRecord.class), any(Integer.class), any(Consumer.class));
    verify(utpManager, never()).generateConnectionId();
  }

  @Test
  public void responseAcceptMessageWithAll1BitListIfContentIsNotStoredLocally() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenReturn(Optional.empty()); // No content on DB
    when(this.utpManager.generateConnectionId()).thenReturn(555);

    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.of(1, 1, 1));
    assertEquals(accept.getConnectionId(), 555);
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), eq(555), any(Consumer.class));
    verify(utpManager, times(1)).generateConnectionId();
  }

  @Test
  public void responseAcceptMessage() {
    Offer offer = new Offer(List.of(DefaultContent.key3));
    when(historyDB.get(ContentKey.decode(DefaultContent.key3)))
        .thenReturn(Optional.of(DefaultContent.value3));
    when(this.utpManager.generateConnectionId()).thenReturn(555);

    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(Bytes.of(1), accept.getContentKeys());
    assertEquals(555, accept.getConnectionId());
    verify(utpManager, times(1)).acceptRead(any(NodeRecord.class), eq(555), any(Consumer.class));
    verify(utpManager, times(1)).generateConnectionId();
  }

  @Test
  public void responseAcceptMessageEmptyIfThereIsAnException() {
    Offer offer = new Offer(List.of(DefaultContent.key1, DefaultContent.key2, DefaultContent.key3));
    when(historyDB.get(any(ContentKey.class))).thenThrow(NullPointerException.class);
    Accept accept = (Accept) historyNetwork.handleOffer(nodeRecord, offer);
    assertEquals(accept.getContentKeys(), Bytes.EMPTY);
  }
}
