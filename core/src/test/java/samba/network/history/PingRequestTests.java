package samba.network.history;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PingRequestTests {

    private static final Bytes pongCustomPayload = Bytes.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f");
    private static final Bytes pingCustomPayload = Bytes.fromHexString("0xfeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    @Test
    public void sendOkPingMessageAndReceiveOkPongTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createPongBytesResponse(pongCustomPayload));
        NodeRecord nodeRecord = createNodeRecord();
        Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

        assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());
        assertEquals(pongCustomPayload, pong.get().getCustomPayload());
        assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
        assertTrue(historyNetwork.isPeerConnected(nodeRecord));
        assertEquals( pong.get().getCustomPayload(),historyNetwork.getRadiusFromNode(nodeRecord));
    }


    @Test
    public void sendOkPingMessageAndReceiveOkEmptyCustomPayloadPongTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createPongBytesResponse(Bytes.EMPTY));
        NodeRecord nodeRecord = createNodeRecord();
        Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

        assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());
        assertEquals(Bytes.EMPTY, pong.get().getCustomPayload());
        assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
        assertFalse(historyNetwork.isPeerConnected(nodeRecord));
        assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
    }

    @Test
    public void handleErrorWhenSendingaPingTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> SafeFuture.failedFuture(new NullPointerException()));
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

        assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
        assertFalse(historyNetwork.isPeerConnected(nodeRecord));
        assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
        assertFalse(pong.isPresent());

    }




    @NotNull
    private static CompletableFuture<Bytes> createPongBytesResponse(Bytes pongCustomPayload ) {
        return CompletableFuture.completedFuture(Bytes.concatenate(Bytes.fromHexString("0x0101000000000000000c000000"),pongCustomPayload));
    }

    @NotNull
    private static Ping createPingMessage() {
        return new Ping(UInt64.valueOf(1), pingCustomPayload);
    }

    private static NodeRecord createNodeRecord(){
        return  new NodeRecordBuilder()
                .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
                .address("12.34.45.67", Integer.parseInt("9001"))
                .build();
    }
}
