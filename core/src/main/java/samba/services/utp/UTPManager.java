package samba.services.utp;

import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;
import samba.utp.UTPClient;
import samba.utp.data.UtpPacket;
import samba.utp.network.TransportLayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class UTPManager implements TransportLayer<UTPAddress> {
  protected static final Logger LOG = LogManager.getLogger();

  private Map<String, UTPClient> connections;
  private Discv5Client discv5Client;
  private NetworkType networkType = NetworkType.UTP;

  public UTPManager(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
    this.connections = new ConcurrentHashMap<>();
  }

  public SafeFuture<Bytes> getContent(NodeRecord nodeRecord, int connectionId) {
    String connectionKey =
        this.createConnectionKey(nodeRecord.asEnr(), String.valueOf(connectionId));
    UTPClient utpClient = this.registerClient(connectionKey);

    return SafeFuture.of(
        utpClient
            .connect(connectionId, new UTPAddress(nodeRecord))
            .thenCompose(__ -> utpClient.read()));
  }

  public void sendContent(NodeRecord nodeRecord, int connectionId, Bytes content) {
    String connectionKey =
        this.createConnectionKey(nodeRecord.asEnr(), String.valueOf(connectionId));
    UTPClient utpClient = this.registerClient(connectionKey);
    ByteBuffer buffer = ByteBuffer.allocate(content.size());
    buffer.put(content.toArray());
    try {
      utpClient
          .startListening(connectionId, new UTPAddress(nodeRecord))
          .thenCompose(__ -> utpClient.write(buffer))
          .get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private UTPClient registerClient(String connectionKey) {
    UTPClient utpClient = new UTPClient(this);
    if (!connections.containsKey(connectionKey)) {
      this.connections.put(connectionKey, utpClient);
      return utpClient;
    }
    return null;
  }

  @Override
  public void sendPacket(UtpPacket packet, UTPAddress remoteAddress) throws IOException {
    SafeFuture.runAsync(
        () ->
            this.discv5Client.sendDisv5Message(
                remoteAddress.getAddress(),
                networkType.getValue(),
                Bytes.of(packet.toByteArray())));
  }

  public void onUTPMessageReceive(NodeRecord nodeRecord, Bytes response) {
    UtpPacket utpPacket = UtpPacket.decode(response);
    UTPClient utpClient = getClient(nodeRecord.asEnr(), utpPacket.getConnectionId());
    if (utpClient != null) {
      utpClient.receivePacket(utpPacket, new UTPAddress(nodeRecord));
    } else {
      LOG.trace("No UTPClient found when receiving packet: {}", utpPacket);
    }
  }

  @Override
  public void close(long connectionId, UTPAddress transportAddress) {
    String connectinKey =
        this.createConnectionKey(
            transportAddress.getAddress().asEnr(), String.valueOf(connectionId));
    this.connections.remove(connectinKey);
  }

  private UTPClient getClient(String enr, int connectionId) {
    String connectionKeyReading = createConnectionKey(enr, String.valueOf(connectionId));
    String connectionKeyOnSync = createConnectionKey(enr, String.valueOf(connectionId - 1));

    UTPClient client = connections.get(connectionKeyReading);
    if (client == null) {
      client = connections.get(connectionKeyOnSync);
    }
    return client;
  }

  private String createConnectionKey(String enr, String connectionId) {
    return enr + "-" + connectionId;
  }
}
