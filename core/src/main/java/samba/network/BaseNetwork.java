package samba.network;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import com.google.common.base.Throwables;

import samba.db.PortalDB;
import samba.domain.messages.PortalWireMessage;
import samba.domain.messages.PortalWireMessageDecoder;
import samba.network.exception.BadRequestException;
import samba.network.exception.MessageToOurselfException;
import samba.network.exception.StoreNotAvailableException;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public abstract class BaseNetwork implements Network {

    protected static final Logger LOG = LogManager.getLogger();

    protected NetworkType networkType;
    protected RoutingTable routingTable;
    protected Discv5Client discv5Client;

    protected UInt64 nodeRadius;
    private PortalDB db;
    // private final PrivKey privKey;
    // private final Host host;

    public BaseNetwork(NetworkType networkType, Discv5Client discv5Client, RoutingTable routingTable, UInt64 nodeRadius) {
        this.networkType = networkType;
        this.discv5Client = discv5Client;
        this.networkType = networkType;
        this.routingTable = routingTable;
        this.nodeRadius = nodeRadius;

    }

    protected SafeFuture<Optional<PortalWireMessage>> sendMessage(NodeRecord destinationNode, PortalWireMessage messageRequest) {
        LOG.trace("Send Portal {} message to {}", messageRequest.getMessageType(), destinationNode.getNodeId());
        if (!isStoreAvailable()) {
            return SafeFuture.failedFuture(new StoreNotAvailableException());
        }
        if(isOurself(destinationNode)){
            return SafeFuture.failedFuture(new MessageToOurselfException());
        }
        //TODO FIX chain order
        return SafeFuture.of(discv5Client.sendDisv5Message(destinationNode, this.networkType.getValue(), messageRequest.getSszBytes())
                        .thenApply((sszbytes)->parseResponse(sszbytes, destinationNode, messageRequest)) //Change
                        .thenApply(Optional::of))
                        .thenPeek(this::logResponse)
                        .exceptionallyCompose(error->handleSendMessageError(messageRequest, error));
    }

    private boolean isOurself(NodeRecord node) {
      return this.discv5Client.getNodeId().isPresent() && this.discv5Client.getNodeId().get().equals(node.getNodeId());
    }

    private boolean isStoreAvailable() {
        return true; //TODO validate store availability
    }

    private void logResponse(Optional<PortalWireMessage> portalWireMessage) {
        portalWireMessage.ifPresent((message)->LOG.trace("Portal {} message received", message.getMessageType()));
    }


    private SafeFuture<Optional<PortalWireMessage>> handleSendMessageError(PortalWireMessage message, Throwable error) {
        LOG.trace("Something when wrong when sending a Portal {} message", message.getMessageType());
        final Throwable rootCause = Throwables.getRootCause(error);
        if (rootCause instanceof IllegalArgumentException) {
            return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
        }
        return SafeFuture.failedFuture(error);
    }

    private PortalWireMessage parseResponse(Bytes sszbytes, NodeRecord destinationNode, PortalWireMessage requestMessage) {
        //TODO validate appropriate response. If I send a Ping I must get a PONG
       return PortalWireMessageDecoder.decode(destinationNode, sszbytes);

    }
}
