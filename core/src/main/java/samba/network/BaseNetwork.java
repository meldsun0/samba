package samba.network;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.db.PortalDB;
import samba.domain.messages.HistoryProtocolReceiveMessage;
import samba.domain.messages.HistoryProtocolRequestMessage;
import samba.domain.messages.sszexample.PingMessageSSZ;
import samba.domain.messages.response.Pong;
import samba.network.exception.BadRequestException;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;

public abstract class BaseNetwork implements Network {

    protected static final Logger LOG = LogManager.getLogger();

    private NetworkType networkType;
    protected RoutingTable routingTable;
    private PortalDB db;
    private Discv5Client client;


    public BaseNetwork(NetworkType networkType, Discv5Client client, RoutingTable routingTable) {
        this.networkType = networkType;
        this.client = client;
        this.networkType = networkType;
        this.routingTable = routingTable;

    }


    // private final PrivKey privKey;
    //  private final NodeId nodeId;
    //   private final Host host;
    // private final PeerManager peerManager;

    protected SafeFuture<Optional<HistoryProtocolReceiveMessage>> sendMessage(NodeRecord node, HistoryProtocolRequestMessage message) {
        LOG.trace("Send {} message to {}", message.getType(), node.getNodeId());
//         if (!isStoreAvailable()) {
//            return SafeFuture.failedFuture(new ChainDataUnavailableException());
//        }
      //  return SafeFuture.of(this.client.sendDisV5Message(node, Bytes.fromHexString("0x500B"),message.getSSZMessageInBytes())
        return SafeFuture.of(client.sendDisV5Message(node, this.networkType.getValue(), Bytes.fromHexString("0x0001000000000000000c000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))
                        .thenApply((bytes)->parseResponse(bytes, node)) //Change
                        .thenApply(Optional::of))
                .exceptionallyCompose(this::handleSendMessageError);
    }


    private SafeFuture<Optional<HistoryProtocolReceiveMessage>> handleSendMessageError(Throwable error) {
        LOG.info("Error when sending a discv5 message");
        final Throwable rootCause = Throwables.getRootCause(error);
        if (rootCause instanceof IllegalArgumentException) {
            return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
        }
        return SafeFuture.failedFuture(error);
    }

    private HistoryProtocolReceiveMessage parseResponse(Bytes response, NodeRecord node) {
       //TODO- add message handler.
        return new Pong(node);
    }

}
