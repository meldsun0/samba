package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.*;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface HistoryNetworkRequestOperations {

    SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message);
    

}
