package samba.services.utp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.services.storage.StorageFactory;
import samba.utp.UTPClient;
import samba.utp.data.UtpPacket;
import samba.utp.network.TransportLayer;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class UTPService extends Service implements TransportLayer<UTPAddress>{
    protected static final Logger LOG = LogManager.getLogger();

    private Map<Integer, UTPClient> connections;
    private Discv5Client discv5Client;
    private NetworkType networkType = NetworkType.UTP;

    public UTPService(final Discv5Client discv5Client) {
        this.discv5Client = discv5Client;
        this.connections = new ConcurrentHashMap<>();
    }

    @Override
    protected SafeFuture<?> doStart() {
        return SafeFuture.fromRunnable(
                () -> {

                });
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }

    public SafeFuture<Bytes> getContent(NodeRecord nodeRecord, int connectionID) {
        UTPClient utpClient = this.registerClient(connectionID);
        return  SafeFuture.of(utpClient
                .connect(connectionID, new UTPAddress("", 111, nodeRecord))
                .thenCompose(__-> utpClient.read())
                .thenApply(result -> Bytes.of(result.array())))
                .thenPeek(result -> LOG.info("Read result from connectionId {}. Result {}", connectionID, result));
    }

    private UTPClient registerClient(int connectionId) {
        UTPClient utpClient = new UTPClient(this);
        if (!connections.containsKey(connectionId)) {
            this.connections.put(connectionId & 0xFFFF, utpClient);
        }
        return utpClient;
        // TODO close if present
    }

    @Override
    public void sendPacket(UtpPacket packet, UTPAddress remoteAddress) throws IOException {
        System.out.println("UTP Sending"+ packet.toString());
        this.discv5Client.sendDisv5Message(remoteAddress.getAddress(), this.networkType.getValue(), Bytes.of(packet.toByteArray())).handle((response, error) ->{
            if(error ==null){
                System.out.println("Sucess"+  response);
            }else{
                System.out.println("error"+ error);
            }
            return null;
        });
    }

    @Override
    public UtpPacket onPacketReceive() throws IOException {
      return null;
    }


    public void onUTPMessageReceive(NodeRecord nodeRecord, Bytes response) {

        UtpPacket utpPacket = UtpPacket.decode(response);
        System.out.println("UTP RESPONSE:"+ utpPacket.toString());
        int connectioNId = utpPacket.getConnectionId();
        this.connections.get(connectioNId).receivePacket(utpPacket);

    }


    @Override
    public void close() {


    }

    //    // IP address + port + Discovery v5 NodeId + connection_id
    //
    //    private final TransportLayer transportLayer;
    //
    //
    //    public CompletableFuture<ByteBuffer> getContent(int connectionId, Bytes nodeRecord) {
    //        UTPClient utpClient = this.registerClient(connectionId);
    //        ByteBuffer buffer = ByteBuffer.allocate(150000000);
    //        return utpClient
    //                .connect(connectionId)
    //                .thenCompose(v -> utpClient.read(buffer))
    //                .thenApply(v ->{
    //                    this.removeClient(connectionId);
    //                    return buffer;
    //                });
    //    }
    //
    //    private UTPClient registerClient(int connectionId) {
    //        UTPClient utpClient = new UTPClient(this.transportLayer);
    //        if (!connections.containsKey(connectionId)) {
    //            this.connections.put(connectionId & 0xFFFF, utpClient);
    //        }
    //        return utpClient;
    //        // TODO close if present
    //    }
    //
    //    private void removeClient(int connectionId) {
    //        connections.remove((int) connectionId & 0xFFFF);
    //    }
    //
    //    public void onPacketReceive(Bytes bytes) {
    ////        UtpPacket utpPacket = UtpPacket.(bytes);
    ////
    ////        UTPClient client = connections.get(utpPacket.getConnectionId() & 0xFFFF);
    ////
    ////        client.receivePacket();
    //    }

}
