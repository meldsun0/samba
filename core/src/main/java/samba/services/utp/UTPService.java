package samba.services.utp;

import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.utp.UTPClient;
import samba.utp.data.UtpPacket;
import samba.utp.network.TransportLayer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class UTPService extends Service implements TransportLayer<UTPAddress> {
  protected static final Logger LOG = LogManager.getLogger();

  //    // IP address + port + Discovery v5 NodeId + connection_id
  private Map<Integer, UTPClient> connections;
  private Discv5Client discv5Client;
  private NetworkType networkType = NetworkType.UTP;

  public UTPService(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
    this.connections = new ConcurrentHashMap<>();
  }

  @Override
  protected SafeFuture<?> doStart() {
    return SafeFuture.fromRunnable(() -> {});
  }

  @Override
  protected SafeFuture<?> doStop() {
    return null;
  }

  public SafeFuture<Bytes> getContent(NodeRecord nodeRecord, int connectionID) {
    UTPClient utpClient = this.registerClient(connectionID);
    return SafeFuture.of(
        utpClient
            .connect(connectionID, new UTPAddress(nodeRecord))
            .thenCompose(__ -> utpClient.read()));
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
    SafeFuture.runAsync(
        () -> {
          LOG.trace("[Sending Packet: " + packet.toString() + "]");
          this.discv5Client.sendDisv5Message(
              remoteAddress.getAddress(), networkType.getValue(), Bytes.of(packet.toByteArray()));
        });
  }


  public void onUTPMessageReceive(NodeRecord nodeRecord, Bytes response) {
    UtpPacket utpPacket = UtpPacket.decode(response);
    int connectionId = utpPacket.getConnectionId();
    this.connections.get(connectionId).receivePacket(utpPacket);
  }

  @Override
  public void close(long connectionId) {
    this.connections.remove((int) connectionId & 0xFFFF);
  }
}
