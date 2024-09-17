package samba.network.history;


import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.*;
import samba.network.BaseNetwork;
import samba.network.NetworkType;

import java.util.concurrent.CompletableFuture;

public class HistoryNetwork extends BaseNetwork  implements HistoryNetworkRequestOperations {



    public HistoryNetwork(){
        super(NetworkType.EXECUTION_HISTORY_NETWORK);
    }


    @Override
    public CompletableFuture ping(Ping pingMessage) { //node should be changed.
        sendMessage(null, pingMessage);
    }

    @Override
    public CompletableFuture findNodes(Nodes nodesMessage) {
        sendMessage(null, nodesMessage);
    }

    @Override
    public CompletableFuture findContent(FindContent findContentMessage) {
        sendMessage(null, findContentMessage);
    }

    @Override
    public CompletableFuture offer(Offer offer) {
        sendMessage(null, offerMessage);
    }
}
