package samba.network.history;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static samba.TestHelper.createNodeRecord;

import samba.storage.HistoryDB;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import samba.services.discovery.Discv5Client;

public class FindNodeMessageTests {

    @Test
    public void findNodes_receiveOkNodesResponse() throws ExecutionException, InterruptedException {
        NodeRecord homeNodeRecord = createNodeRecord();
        Discv5Client discv5Client = mockDiscv5Client(homeNodeRecord, Bytes.fromHexString("0x030105000000080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235"));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client,  mock(HistoryDB.class));
        Nodes nodes = historyNetwork.findNodes(createNodeRecord(), new FindNodes(Set.of(256, 255))).get().get();

        List<String> enrList = List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg", "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");

        assertEquals(1, nodes.getTotal());
        assertEquals(enrList, nodes.getEnrList().stream().map(enr -> enr.replace("=", "")).collect(Collectors.toList()));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(3)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));
    }


    @Test
    public void findNodes_receivesNodesWithHomeNodeSoNoPingsShouldBeTriggered() throws ExecutionException, InterruptedException {
        NodeRecord homeNodeRecord = createNodeRecord();

        Discv5Client discv5Client = mockDiscv5Client(homeNodeRecord, List.of(homeNodeRecord.asBase64()));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
        Nodes nodes = historyNetwork.findNodes(createNodeRecord(), new FindNodes(Set.of(256, 255))).get().get();

        assertEquals(1, nodes.getTotal());
        assertEquals(homeNodeRecord.asBase64(), nodes.getEnrList().getFirst().replace("=", ""));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(1)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));

    }


    @Test
    public void findNodes_receivesNodesWithHomeNodeAndRequestingNodeSoNoPingsShouldBeTriggered() throws ExecutionException, InterruptedException {
        NodeRecord homeNodeRecord = createNodeRecord();
        NodeRecord requestingNodeRecord = createNodeRecord();

        Discv5Client discv5Client = mockDiscv5Client(homeNodeRecord, List.of(homeNodeRecord.asBase64(), requestingNodeRecord.asBase64()));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
        Nodes nodes = historyNetwork.findNodes(requestingNodeRecord, new FindNodes(Set.of(256, 255))).get().get();

        assertEquals(1, nodes.getTotal());
        assertEquals(homeNodeRecord.asBase64(), nodes.getEnrList().getFirst().replace("=", ""));
        assertEquals(requestingNodeRecord.asBase64(), nodes.getEnrList().getLast().replace("=", ""));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(1)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));

    }


    @Test
    public void findNodes_receivesNodesWithHomeNodeAndRequestingNodeButWithANewOneSoAPingsShouldBeTriggered() throws ExecutionException, InterruptedException {
        NodeRecord homeNodeRecord = createNodeRecord();
        NodeRecord newHomeRecord = createNodeRecord();

        Discv5Client discv5Client = mockDiscv5Client(homeNodeRecord, List.of(homeNodeRecord.asBase64(), newHomeRecord.asBase64()));

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));
        Nodes nodes = historyNetwork.findNodes(createNodeRecord(), new FindNodes(Set.of(256, 255))).get().get();

        assertEquals(1, nodes.getTotal());
        assertEquals(homeNodeRecord.asBase64(), nodes.getEnrList().getFirst().replace("=", ""));
        assertEquals(newHomeRecord.asBase64(), nodes.getEnrList().getLast().replace("=", ""));

        assertTrue(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS));
        verify(discv5Client, times(2)).sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class));

    }

    @Test
    public void handle_find_nodes_receivesDistanceZero() {
        NodeRecord homeNodeRecord = createNodeRecord();
        Discv5Client discv5Client = mockDiscv5Client(homeNodeRecord, List.of());
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, mock(HistoryDB.class));

        PortalWireMessage portalWireMessage = historyNetwork.handleFindNodes(createNodeRecord(), new FindNodes(Set.of(0)));


        assertEquals(MessageType.NODES, portalWireMessage.getMessageType());
        assertEquals(homeNodeRecord.asEnr(), ((Nodes)portalWireMessage.getMessage()).getEnrList().getFirst());
    }

    

    @NotNull
    private static Discv5Client mockDiscv5Client(NodeRecord homeNodeRecord, List<String> enrs) {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createNodeBytesResponse((new Nodes(enrs)).getSszBytes()));
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
        return discv5Client;
    }

    @NotNull
    private static Discv5Client mockDiscv5Client(NodeRecord homeNodeRecord, Bytes nodesPayload) {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenAnswer(invocation -> createNodeBytesResponse(nodesPayload));
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
        return discv5Client;
    }


    @NotNull
    private static CompletableFuture<Bytes> createNodeBytesResponse(Bytes nodesMessage) {
        return CompletableFuture.completedFuture(nodesMessage);
    }

}
