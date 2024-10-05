package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.NodesV2;
import samba.domain.messages.response.Pong;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;


public interface HistoryNetworkRequestOperations {

    SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message);


     SafeFuture<Optional<NodesV2>> findNodes(NodeRecord nodeRecord, FindNodes findNodes);
    

}
