package samba.network;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import samba.db.PortalDB;
import samba.domain.messages.HistoryProtocolReceiveMessage;
import samba.domain.messages.HistoryProtocolRequestMessage;
import samba.domain.messages.response.Pong;
import samba.network.exception.BadRequestException;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;

public abstract class BaseNetwork {

    protected static final Logger LOG = LogManager.getLogger();

    private NetworkType networkType;
    protected RoutingTable routingTable;
    private PortalDB db;
    private PeerClient client;


    public BaseNetwork(NetworkType networkType, PeerClient client) {
        this.networkType = networkType;
        this.client = client;

    }


    // private final PrivKey privKey;
    //  private final NodeId nodeId;
    //   private final Host host;
    // private final PeerManager peerManager;

    protected SafeFuture<Optional<HistoryProtocolReceiveMessage>> sendMessage(NodeRecord node, HistoryProtocolRequestMessage message) {
//       if (!isStoreAvailable()) {
//            return SafeFuture.failedFuture(new ChainDataUnavailableException());
//        }

        return  SafeFuture.of(client.sendMessage(node, this.networkType.getValue(), message.getSSZMessageInBytes())
                        .thenApply(this::parseInput)
                        .thenApply(Optional::of))
                    .exceptionallyCompose(this::handleSendMessageError);

    }


    private  SafeFuture<Optional<HistoryProtocolReceiveMessage>> handleSendMessageError(Throwable error) {
        final Throwable rootCause = Throwables.getRootCause(error);
        if (rootCause instanceof IllegalArgumentException) {
            return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
        }
        return SafeFuture.failedFuture(error);
    }

    private HistoryProtocolReceiveMessage parseInput(Bytes input){
        HistoryProtocolReceiveMessage a = new Pong();
            return  a;
    }



}
