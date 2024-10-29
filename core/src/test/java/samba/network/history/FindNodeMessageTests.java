package samba.network.history;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import samba.TestHelper;
import samba.domain.messages.MessageType;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static samba.TestHelper.createNodeRecord;

public class FindNodeMessageTests {

    /*
    @Test
    public void sendOkFindNodeMessageAndReceiveOkNodeTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        NodeRecord homeNodeRecord = createNodeRecord();

        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createNodeBytesResponse());
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);

        NodeRecord nodeRecord = createNodeRecord();
        Optional<Nodes> nodes = historyNetwork.findNodes(nodeRecord, createFindNodeMessage()).get();

        List<String> enrList = List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg", "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");

        assertEquals(1, nodes.get().getTotal());
        assertEquals(enrList, nodes.get().getEnrList().stream().map(enr -> enr.replace("=", "")).collect(Collectors.toList()));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(3)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));
    }
/*

    @Test
    public void sendOkFindNodeMessageAndReceiveOkNodeWithHomeNodeTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        NodeRecord homeNodeRecord = createNodeRecord();

        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createNodeBytesResponse());
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);

        NodeRecord nodeRecord = createNodeRecord();
        Optional<Nodes> nodes = historyNetwork.findNodes(nodeRecord, createFindNodeMessage()).get();

        List<String> enrList = List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg", "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");

        assertEquals(1, nodes.get().getTotal());
        assertEquals(enrList, nodes.get().getEnrList().stream().map(enr -> enr.replace("=", "")).collect(Collectors.toList()));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(3)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));
    }


//    @Test
//    public void pingNewNodes_testRemovingHomeNodeFromListOfNodes() throws ExecutionException, InterruptedException {
//        Discv5Client discv5Client = mock(Discv5Client.class);
//        NodeRecord homeNodeRecord = createNodeRecord();
//        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
//        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
//
//        NodeRecord nodeRecord = createNodeRecord();
//        Optional<Nodes> nodes = historyNetwork.findNodes(nodeRecord, createFindNodeMessage()).get();
//
//        List<String> enrList = List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg", "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");
//
//        assertEquals(1, nodes.get().getTotal());
//        assertEquals(enrList, nodes.get().getEnrList().stream().map(enr -> enr.replace("=", "")).collect(Collectors.toList()));
//    }


    @Test
    public void sendOkPingMessageAndReceiveOkEmptyCustomPayloadPongTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createPongBytesResponse(Bytes.EMPTY));
        when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
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
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> SafeFuture.failedFuture(new NullPointerException()));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Pong> pong = historyNetwork.ping(nodeRecord, createPingMessage()).get();

        assertEquals(0, historyNetwork.getNumberOfConnectedPeers());
        assertFalse(historyNetwork.isNodeConnected(nodeRecord));
        assertNull(historyNetwork.getRadiusFromNode(nodeRecord));
        assertFalse(pong.isPresent());

    }


    @Test
    public void handleASuccessfulPingIncomingRequestTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createPongBytesResponse(pongCustomPayload));
        when(discv5Client.getEnrSeq()).thenAnswer(invocation -> org.apache.tuweni.units.bigints.UInt64.valueOf(1));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        NodeRecord nodeRecord = TestHelper.createNodeRecord();
        Ping pingMessage = createPingMessage();

        historyNetwork.handlePing(nodeRecord, createPingMessage() );


        assertEquals(1, historyNetwork.getNumberOfConnectedPeers());
        assertTrue(historyNetwork.isNodeConnected(nodeRecord));
        assertEquals(pingMessage.getCustomPayload(), historyNetwork.getRadiusFromNode(nodeRecord));
    }


    @NotNull
    private static CompletableFuture<Bytes> createNodeBytesResponse(Bytes nodesListCustomPayload ) {
        return CompletableFuture.completedFuture(nodesList);
    }
 */

    private static FindNodes createFindNodeMessage() {
        return new FindNodes(Set.of(256, 255));
    }

}
