package samba.network.history;


import jnr.a64asm.OP;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import samba.domain.messages.MessageType;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.domain.node.Node;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PingOperationTests {

    private static final Bytes pongCustomPayload = Bytes.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f");
    private static final Bytes pingCustomPayload = Bytes.fromHexString("0xfeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    @Test
    public void sendOkPingMessageTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        when(discv5Client.sendDisV5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createPongBytesResponse());
        Optional<Pong> pong = historyNetwork.ping(mock(NodeRecord.class), createPingMessage()).get();

        assertEquals(UInt64.valueOf(1), pong.get().getEnrSeq());

        assertEquals(pongCustomPayload, pong.get().getCustomPayload());
        assertEquals(1, historyNetwork.getPeerCount());
    }

    @NotNull
    private static CompletableFuture<Bytes> createPongBytesResponse() {
        return CompletableFuture.completedFuture(Bytes.concatenate(Bytes.fromHexString("0x0101000000000000000c000000"),pongCustomPayload));
    }

    @NotNull
    private static Ping createPingMessage() {
        return new Ping(UInt64.valueOf(1), pingCustomPayload);
    }
}
