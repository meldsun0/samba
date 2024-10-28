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
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.response.Content;
import samba.services.discovery.Discv5Client;

public class FindContentMessageTests {
    
    private static final Bytes contentKey = Bytes.fromHexString("0x706f7274616c");

    private HistoryDB historyDB;

    @BeforeEach
    public void setUp() {
        //Create Discv5Client
        //Create HistoryNetwork
        //populate mock database

        this.historyDB = mock(HistoryDB.class);
        
        

    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentConnectionIdTest() throws ExecutionException, InterruptedException{
        Discv5Client discv5Client = mock(Discv5Client.class);
        when(discv5Client.sendDisv5Message(any(NodeRecord.class), any(Bytes.class), any(Bytes.class))).thenReturn(createContentConnectionIdBytesResponse(1234));
        
        HistoryNetwork historyNetwork = new HistoryNetwork(discv5Client);
        NodeRecord nodeRecord = createNodeRecord();

        Optional<Content> content = historyNetwork.findContent(nodeRecord, createFindContentMessage(contentKey)).get();

        assertEquals(1234, content.get().getConnectionId());
    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentContentTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveOkContentEnrTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentConnectionIdTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentContentTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveEmptyContentEnrTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentPacketTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentConnectionIdTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentContentTest() {

    }

    @Test
    public void sendOkFindContentMessageAndRecieveBadContentEnrTest() {

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

    private static FindContent createFindContentMessage(Bytes contentKey) {
        return new FindContent(contentKey);
    }
}