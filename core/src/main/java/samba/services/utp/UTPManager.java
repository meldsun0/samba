package samba.services.utp;

import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import meldsun0.utp.UTPClient;
import meldsun0.utp.data.UtpPacket;
import meldsun0.utp.network.TransportLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

@SuppressWarnings("SameNameButDifferent")
public class UTPManager implements TransportLayer<UTPAddress> {
  protected static final Logger LOG = LogManager.getLogger();

  private final Map<String, UTPClient> connections;
  private final Discv5Client discv5Client;

  public UTPManager(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
    this.connections = new ConcurrentHashMap<>();
  }

  public int acceptRead(NodeRecord nodeRecord, Consumer<Bytes> onContentReceived) {
    int connectionId = UTPClient.generateRandomConnectionId();
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .startListening(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.read())
              .thenAccept(onContentReceived)
              .get();
        },
        "acceptRead",
        nodeRecord,
        connectionId);
    return connectionId;
  }

  public void offerWrite(final NodeRecord nodeRecord, final int connectionId, Bytes content) {
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .connect(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.write(content))
              .get();
        },
        "offerWrite",
        nodeRecord,
        connectionId);
  }

  public int foundContentWrite(NodeRecord nodeRecord, Bytes content) {
    int connectionId = UTPClient.generateRandomConnectionId();
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .startListening(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.write(content))
              .get();
        },
        "foundContentWrite",
        nodeRecord,
        connectionId);
    return connectionId;
  }

  public SafeFuture<Bytes> findContentRead(NodeRecord nodeRecord, int connectionId) {
    return SafeFuture.of(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          return utpClient
              .connect(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.read());
        });
  }

  @Override
  public void close(long connectionId, UTPAddress transportAddress) {
    String connectionKey =
        this.createConnectionKey(
            transportAddress.getAddress().asEnr(), String.valueOf(connectionId));
    this.connections.remove(connectionKey);
  }

  @Override
  public void sendPacket(UtpPacket packet, UTPAddress remoteAddress) throws IOException {
    SafeFuture.runAsync(
        () ->
            this.discv5Client.sendDisv5Message(
                remoteAddress.getAddress(), NetworkType.UTP.getValue(), packet.toBytes()));
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

  private UTPClient getClient(String enr, int connectionId) {
    String connectionKeyReading = createConnectionKey(enr, String.valueOf(connectionId));
    String connectionKeyOnSync = createConnectionKey(enr, String.valueOf(connectionId - 1));

    UTPClient client = connections.get(connectionKeyReading);
    if (client == null) {
      client = connections.get(connectionKeyOnSync);
    }
    return client;
  }

  public UTPClient registerClient(final NodeRecord nodeRecord, final int connectionId)
      throws UTPClientRegistrationException {
    String connectionKey =
        this.createConnectionKey(nodeRecord.asEnr(), String.valueOf(connectionId));
    if (!connections.containsKey(connectionKey)) {
      UTPClient utpClient = new UTPClient(this);
      this.connections.put(connectionKey, utpClient);
      return utpClient;
    }
    throw new UTPClientRegistrationException(
        "Client with connection key " + connectionKey + " already registered.");
  }

  private String createConnectionKey(String enr, String connectionId) {
    return enr + "-" + connectionId;
  }

  private static <V> Function<Throwable, V> defaultUTPErrorLog(
      String operationName, NodeRecord nodeRecord, int connectionId) {
    return error -> {
      defaultUTPErrorLog(operationName, nodeRecord, connectionId, error);
      return null;
    };
  }

  private static void defaultUTPErrorLog(
      String operationName, NodeRecord nodeRecord, int connectionId, Throwable error) {
    LOG.trace(
        "Error {} when {} from {} on connectionId {}",
        error.getClass().getSimpleName(),
        operationName,
        nodeRecord,
        connectionId,
        error);
  }

  private void runAsyncUTP(
      RunnableUTP task, String operationName, NodeRecord nodeRecord, int connectionId) {
    SafeFuture.runAsync(() -> executeWithHandling(task, operationName, nodeRecord, connectionId))
        .exceptionally(defaultUTPErrorLog(operationName, nodeRecord, connectionId));
  }

  private void executeWithHandling(
      RunnableUTP task, String operationName, NodeRecord nodeRecord, int connectionId) {
    try {
      task.run();
    } catch (InterruptedException | ExecutionException | UTPClientRegistrationException e) {
      defaultUTPErrorLog(operationName, nodeRecord, connectionId, e);
    }
  }

  @FunctionalInterface
  interface RunnableUTP {
    void run() throws InterruptedException, ExecutionException, UTPClientRegistrationException;
  }
}
