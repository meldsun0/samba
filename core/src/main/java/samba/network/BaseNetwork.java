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
import samba.network.exception.StoreNotAvailableException;
import samba.services.discovery.Discv5Client;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public abstract class BaseNetwork implements Network {

    protected static final Logger LOG = LogManager.getLogger();

    private NetworkType networkType;
    protected RoutingTable routingTable;
    private Discv5Client client;

    private NodeRecord nodeRecord;
    protected UInt64 nodeRadius;
    private PortalDB db;
    // private final PrivKey privKey;
    // private final Host host;

    public BaseNetwork(NetworkType networkType, Discv5Client client, RoutingTable routingTable, UInt64 nodeRadius) {
        this.networkType = networkType;
        this.client = client;
        this.networkType = networkType;
        this.routingTable = routingTable;
        this.nodeRadius = nodeRadius;

    }

    protected SafeFuture<Optional<PortalWireMessage>> sendMessage(NodeRecord node, PortalWireMessage messageRequest) {
        LOG.trace("Send {} message to {}", messageRequest.getMessageType(), node.getNodeId());
         if (!isStoreAvailable()) {
            return SafeFuture.failedFuture(new StoreNotAvailableException());
        }
//        if(nodeRecord.equals(node)){
//            return SafeFuture.failedFuture(new MessageToOurselfException());
//        }
        //TODO FIX chain order
        return SafeFuture.of(client.sendDisV5Message(node, this.networkType.getValue(), messageRequest.getSszBytes())
                        .thenApply((sszbytes)->parseResponse(sszbytes, node, messageRequest)) //Change
                        .thenApply(Optional::of))
                        .thenPeek(this::logResponse)
                        .exceptionallyCompose(error->handleSendMessageError(messageRequest, error));
    }

    private boolean isStoreAvailable() {
        return true; //TODO validate store availability
    }

    private void logResponse(Optional<PortalWireMessage> portalWireMessage) {
        portalWireMessage.ifPresent((message)->LOG.info("{} message received", message.getMessageType()));
    }


    private SafeFuture<Optional<PortalWireMessage>> handleSendMessageError(PortalWireMessage message, Throwable error) {
        LOG.info("Something when wrong when sending a {} message", message.getMessageType());
        final Throwable rootCause = Throwables.getRootCause(error);
        if (rootCause instanceof IllegalArgumentException) {
            return SafeFuture.failedFuture(new BadRequestException(rootCause.getMessage()));
        }
        return SafeFuture.failedFuture(error);
    }

    private PortalWireMessage parseResponse(Bytes sszbytes, NodeRecord node, PortalWireMessage requestMessage) {
        //TODO validate appropriate response. If I send a Ping I must get a PONG
        return PortalWireMessageDecoder.decode(node, sszbytes);
    }

}
