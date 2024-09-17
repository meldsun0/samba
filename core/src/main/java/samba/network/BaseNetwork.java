package samba.network;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.db.PortalDB;
import samba.domain.messages.MessageType;
import samba.domain.messages.ProtocolMessage;

import java.util.concurrent.CompletableFuture;

public abstract class BaseNetwork {

    private NetworkType networkType;
    private RoutingTable routingTable;
    private PortalDB db;
    private PeerClient client;


    public BaseNetwork(NetworkType networkType) {
        this.networkType = networkType;

    }


    // private final PrivKey privKey;
    //  private final NodeId nodeId;
    //   private final Host host;
    // private final PeerManager peerManager;

    protected  final  CompletableFuture sendMessage(NodeRecord node, ProtocolMessage message) {
        //validate stuff
           return client.sendMessage(node, networkType.getValue(), message.getSSZMessageInBytes());

    }



}
