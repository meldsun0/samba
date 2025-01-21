package samba.services.utp;

import java.util.concurrent.CompletionStage;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class UTPService extends Service {

  //  private UTPManager utpManager;

  public UTPService() {
    //  this.utpManager = new UTPManager();
  }

  @Override
  protected SafeFuture<?> doStart() {
    return null;
  }

  @Override
  protected SafeFuture<?> doStop() {
    return null;
  }

  public CompletionStage<Bytes> getContent(NodeRecord nodeRecord, int connectionID) {
    // return this.utpManager.getContent(connectionID);
    return null;
  }

  //    // IP address + port + Discovery v5 NodeId + connection_id
  //    private final Map<Integer, UTPClient> connections = new HashMap<>();
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
