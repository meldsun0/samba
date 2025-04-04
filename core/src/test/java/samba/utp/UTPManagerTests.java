package samba.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPClientRegistrationException;
import samba.services.utp.UTPManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import meldsun0.utp.UTPClient;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class UTPManagerTests {

  private Discv5Client discv5Client;
  private UTPClient utpClient;
  private UTPManager utpManager;
  private NodeRecord nodeRecord;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    this.utpManager = spy(new UTPManager(discv5Client));
    this.utpClient = mock(UTPClient.class);
    this.nodeRecord = mock(NodeRecord.class);
  }

  @Test
  void testAcceptRead() throws UTPClientRegistrationException {
    Consumer<Bytes> contentConsumer = mock(Consumer.class);

    doReturn(utpClient).when(utpManager).registerClient(any(), anyInt());
    doReturn(CompletableFuture.completedFuture(null))
        .when(utpClient)
        .startListening(anyInt(), any());
    doReturn(CompletableFuture.completedFuture(Bytes.of(1, 2, 3))).when(utpClient).read(any());

    int connectionId = utpManager.acceptRead(nodeRecord, contentConsumer);
    assertTrue(connectionId > 0);

    ArgumentCaptor<Bytes> captor = ArgumentCaptor.forClass(Bytes.class);
    verify(contentConsumer, timeout(1000)).accept(captor.capture());
    assertEquals(captor.getValue(), Bytes.of(1, 2, 3));
  }

  @Test
  void testOfferWrite() throws UTPClientRegistrationException {
    Bytes content = Bytes.of(1, 2, 3);
    int connectionId = 1234;

    doReturn(utpClient).when(utpManager).registerClient(any(), anyInt());
    doReturn(CompletableFuture.completedFuture(null)).when(utpClient).connect(anyInt(), any());
    doReturn(CompletableFuture.completedFuture(null))
        .when(utpClient)
        .write(content, Executors.newSingleThreadExecutor());

    assertDoesNotThrow(() -> utpManager.offerWrite(nodeRecord, connectionId, content));
  }

  @Test
  void testFoundContentWrite() throws UTPClientRegistrationException {
    Bytes content = Bytes.of(4, 5, 6);

    doReturn(utpClient).when(utpManager).registerClient(any(), anyInt());
    doReturn(CompletableFuture.completedFuture(null))
        .when(utpClient)
        .startListening(anyInt(), any());
    doReturn(CompletableFuture.completedFuture(null))
        .when(utpClient)
        .write(content, Executors.newSingleThreadExecutor());

    int connectionId = utpManager.foundContentWrite(nodeRecord, content);
    assertTrue(connectionId > 0);
  }

  @Test
  void testFindContentRead()
      throws UTPClientRegistrationException, ExecutionException, InterruptedException {
    int connectionId = 5678;
    Bytes expectedContent = Bytes.of(7, 8, 9);

    doReturn(utpClient).when(utpManager).registerClient(any(), anyInt());
    doReturn(CompletableFuture.completedFuture(null)).when(utpClient).connect(anyInt(), any());
    doReturn(CompletableFuture.completedFuture(expectedContent)).when(utpClient).read(any());

    Bytes content = utpManager.findContentRead(nodeRecord, connectionId).get();
    assertEquals(expectedContent, content);
  }
}
