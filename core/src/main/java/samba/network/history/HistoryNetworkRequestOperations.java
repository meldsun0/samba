package samba.network.history;

import org.ethereum.beacon.discovery.message.PingMessage;
import samba.domain.messages.*;
import samba.network.ProtocolRequest;

import java.util.concurrent.CompletableFuture;

public interface HistoryNetworkRequestOperations<T> {

    CompletableFuture<T> ping(Ping ping);

    CompletableFuture<T> findNodes(Nodes message);

    CompletableFuture<T> findContent(FindContent message);

    CompletableFuture<T> offer(Offer offer);
}
