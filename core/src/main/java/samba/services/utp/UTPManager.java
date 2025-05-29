package samba.services.utp;

import samba.metrics.SambaMetricCategory;
import samba.network.NetworkType;
import samba.services.discovery.Discv5Client;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import meldsun0.utp.UTPClient;
import meldsun0.utp.data.UtpPacket;
import meldsun0.utp.network.TransportLayer;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

@SuppressWarnings("SameNameButDifferent")
public class UTPManager implements TransportLayer<UTPAddress> {

  private static final Logger LOG = LoggerFactory.getLogger(UTPManager.class);

  private final Map<String, UTPClient> connections;
  private final Discv5Client discv5Client;

  private final ExecutorService utpExecutor = Executors.newVirtualThreadPerTaskExecutor();

  public UTPManager(final Discv5Client discv5Client, final MetricsSystem metricsSystem) {
    this.discv5Client = discv5Client;
    this.connections = new ConcurrentHashMap<>();

    metricsSystem.createIntegerGauge(
        SambaMetricCategory.HISTORY,
        "utp_active_connection_count",
        "Current UTP connections",
        this.connections::size);
  }

  public int acceptRead(NodeRecord nodeRecord, Consumer<Bytes> onContentReceived) {
    int connectionId = UTPClient.generateRandomConnectionId();
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .startListening(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.read(this.utpExecutor))
              .thenAccept(onContentReceived)
              .exceptionallyCompose(
                  error -> {
                    defaultUTPErrorLog("acceptRead", nodeRecord, connectionId, error);
                    return SafeFuture.completedFuture(null);
                  })
              .get();
        },
        "acceptRead",
        nodeRecord,
        connectionId,
        this.utpExecutor);
    return connectionId;
  }

  public void offerWrite(final NodeRecord nodeRecord, final int connectionId, Bytes content) {
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .connect(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.write(content, this.utpExecutor))
              .get();
        },
        "offerWrite",
        nodeRecord,
        connectionId,
        this.utpExecutor);
  }

  public int foundContentWrite(NodeRecord nodeRecord, Bytes content) {
    int connectionId = UTPClient.generateRandomConnectionId();
    this.runAsyncUTP(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          utpClient
              .startListening(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.write(content, this.utpExecutor))
              .get();
        },
        "foundContentWrite",
        nodeRecord,
        connectionId,
        this.utpExecutor);
    return connectionId;
  }

  public SafeFuture<Bytes> findContentRead(NodeRecord nodeRecord, int connectionId) {
    return SafeFuture.of(
        () -> {
          UTPClient utpClient = this.registerClient(nodeRecord, connectionId);
          return utpClient
              .connect(connectionId, new UTPAddress(nodeRecord))
              .thenCompose(__ -> utpClient.read(this.utpExecutor));
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
            this.discv5Client.sendDiscv5Message(
                remoteAddress.getAddress(), NetworkType.UTP.getValue(), packet.toBytes()),
        this.utpExecutor);
  }

  public void onUTPMessageReceive(NodeRecord nodeRecord, Bytes response) {
    UtpPacket utpPacket = UtpPacket.decode(response);
    UTPClient utpClient = getClient(nodeRecord.asEnr(), utpPacket.getConnectionId());
    if (utpClient != null) {
      utpClient.receivePacket(utpPacket, new UTPAddress(nodeRecord));
    } else {
      LOG.error("No UTPClient found when receiving packet: {}", utpPacket);
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
    LOG.error(
        "Error {} when {} from {} on connectionId {}",
        error.getClass().getSimpleName(),
        operationName,
        nodeRecord,
        connectionId,
        error);
  }

  private void runAsyncUTP(
      RunnableUTP task,
      String operationName,
      NodeRecord nodeRecord,
      int connectionId,
      Executor executor) {
    SafeFuture.runAsync(
            () -> executeWithHandling(task, operationName, nodeRecord, connectionId), executor)
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
