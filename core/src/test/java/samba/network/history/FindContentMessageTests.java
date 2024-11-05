package samba.network.history;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static samba.TestHelper.createNodeRecord;
import samba.db.history.HistoryDB;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.response.Content;
import samba.services.discovery.Discv5Client;

public class FindContentMessageTests {
    
    private static final Bytes contentKey = Bytes.fromHexString("0x706f7274616c");

    private HistoryDB historyDB;

    @BeforeEach
    public void setUp() {
        this.historyDB = mock(HistoryDB.class);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentConnectionIdTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createContentConnectionIdBytesResponse(1234));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
        
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(1234, content.get().getConnectionId());
    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentContentTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createContentContentBytesResponse(Bytes.fromHexString("0x1234567890")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Bytes.fromHexString("0x1234567890"), content.get().getContent());
    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentEnrTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createContentEnrBytesResponse(List.of("-LI=", "-LI=", "-LI=")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(List.of("-LI=", "-LI=", "-LI="), content.get().getEnrList());
    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentConnectionIdTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0500")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Optional.empty(), content);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentContentTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0501")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Optional.empty(), content);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentEnrTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0502")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(List.of(), content.get().getEnrList());
    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentPacketTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x05FFFFFFFF")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Optional.empty(), content);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentConnectionIdTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0500FFFFFFFF")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Optional.empty(), content);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentContentTest() throws ExecutionException, InterruptedException {
        byte[] largeContent = new byte[PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES + 1];
        for (int i = 0; i < PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES + 1; i++) largeContent[i] = (byte) 0xFF;

        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.wrap(largeContent)));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(Optional.empty(), content);
    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentEnrTest() throws ExecutionException, InterruptedException {
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createBadContentBytesResponse(Bytes.fromHexString("0x0502FFFFFFFF")));
        NodeRecord homeNodeRecord = createNodeRecord();
        when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client, historyDB);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

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