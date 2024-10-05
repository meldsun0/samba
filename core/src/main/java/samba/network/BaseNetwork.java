package samba.network;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.db.PortalDB;
import samba.domain.messages.PortalWireMessage;
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

    protected SafeFuture<Optional<PortalWireMessage>> sendMessage(NodeRecord node, PortalWireMessage message) {
        LOG.trace("Send {} message to {}", message.getMessageType(), node.getNodeId());
//         if (!isStoreAvailable()) {
//            return SafeFuture.failedFuture(new ChainDataUnavailableException());
//        }
      //  return SafeFuture.of(this.client.sendDisV5Message(node, Bytes.fromHexString("0x500B"),message.getSSZMessageInBytes())
        return SafeFuture.of(client.sendDisV5Message(node, this.networkType.getValue(), Bytes.fromHexString("0x0001000000000000000c000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))
                        .thenApply((bytes)->parseResponse(bytes, node)) //Change
                        .thenApply(Optional::of))
                        .thenPeek(this::logResponse)
                        .exceptionallyCompose(this::handleSendMessageError);
    }

    private void logResponse(Optional<PortalWireMessage> portalWireMessage) {
        portalWireMessage.ifPresent((message)->LOG.info("{} message received", message.getMessageType()));
    }


    private SafeFuture<Optional<PortalWireMessage>> handleSendMessageError(Throwable error) {
        LOG.info("Error when sending a discv5 message");
        final Throwable rootCause = Throwables.getRootCause(error);
        if (rootCause instanceof IllegalArgumentException) {
            return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
        }
        return SafeFuture.failedFuture(error);
    }

    private PortalWireMessage parseResponse(Bytes response, NodeRecord node) {
       //TODO- add message handler and validate that each response is from the corresponding request
        return new Pong(node);
    }

}
