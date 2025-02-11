package samba.utp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static samba.utp.SessionState.*;
import static samba.utp.data.UtpPacketUtils.*;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;
import static samba.utp.message.MessageUtil.*;

import samba.utp.algo.UtpAlgConfiguration;
import samba.utp.data.MicroSecondsTimeStamp;
import samba.utp.data.SelectiveAckHeaderExtension;
import samba.utp.data.UtpPacket;
import samba.utp.data.util.Utils;
import samba.utp.message.*;
import samba.utp.network.TransportAddress;
import samba.utp.network.TransportLayer;
import samba.utp.operations.UTPReadingFuture;
import samba.utp.operations.UTPWritingFuture;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

public class UTPClient {

  private static final Logger LOG = LogManager.getLogger(UTPClient.class);
  private int connectionIdMASK = 0xFFFF;

  private Session session;

  private final BlockingQueue<UtpTimestampedPacketDTO> queue =
      new LinkedBlockingQueue<UtpTimestampedPacketDTO>();

  private MicroSecondsTimeStamp timeStamper = new MicroSecondsTimeStamp();
  private ScheduledExecutorService retryConnectionTimeScheduler;

  private Optional<UTPWritingFuture> writer = Optional.empty();
  private Optional<UTPReadingFuture> reader = Optional.empty();

  private AtomicBoolean listen = new AtomicBoolean(false);

  private CompletableFuture<Void> connection;
  private final TransportLayer transportLayer;

  public UTPClient(final TransportLayer transportLayer) {
    this.session = new Session();
    this.transportLayer = transportLayer;
    this.connection = new CompletableFuture<>();
  }

  public CompletableFuture<Void> connect(int connectionId, TransportAddress transportAddress) {
    checkNotNull(transportAddress, "Address");
    checkArgument(Utils.isConnectionValid(connectionId), "ConnectionId invalid number");

    LOG.info("Start Client receiving from  {}", connectionId);

    if (!listen.compareAndSet(false, true)) {
      CompletableFuture.failedFuture(
          new IllegalStateException(
              "Attempted to start an already started server listening on " + connectionId));
    }

    try {
      this.session.initConnection(transportAddress, connectionId);

      UtpPacket message =
          buildSYNMessage(
              timeStamper.utpTimeStamp(),
              connectionId,
              UtpAlgConfiguration.MAX_PACKET_SIZE * 1000L);
      sendPacket(message);
      this.session.updateStateOnConnectionInitSuccess();
      startConnectionTimeOutCounter(message);
      this.session.printState();
    } catch (IOException exp) {
      // TODO reconnect
    }
    return connection;
  }

  public CompletableFuture<Void> startListening(
      int connectionId, TransportAddress transportAddress) {
    checkNotNull(transportAddress, "Address");
    checkArgument(Utils.isConnectionValid(connectionId), "ConnectionId invalid number");
    LOG.info("Start server sending to  {}", connectionId);
    if (!listen.compareAndSet(false, true)) {
      CompletableFuture.failedFuture(
          new IllegalStateException(
              "Attempted to start an already started server listening on " + connectionId));
    }
    this.session.initServerConnection(transportAddress, connectionId);
    this.session.printState();
    return connection;
  }

  public void receivePacket(UtpPacket utpPacket, TransportAddress transportAddress) {
    LOG.info("[Receiving Packet: " + utpPacket.toString() + "]");
    switch (utpPacket.getMessageType()) {
      case ST_RESET -> this.stop();
      case ST_SYN -> handleIncommingConnectionRequest(utpPacket, transportAddress);
      case ST_DATA, ST_STATE -> queuePacket(utpPacket);
      case ST_FIN -> handleFinPacket(utpPacket);
      default -> sendResetPacket();
    }
  }

  private void handleIncommingConnectionRequest(
      UtpPacket utpPacket, TransportAddress transportAddress) {
    if (this.session.getState() == CLOSED
        || (this.session.getState() == CONNECTED
            && ((utpPacket.getConnectionId() & 0xFFFFFFFF)
                    == this.session.getConnectionIdReceiving()
                && transportAddress.equals(this.session.getRemoteAddress())))) {
      try {
        this.session.updateStateOnConnectionInitSuccess(utpPacket.getSequenceNumber());
        session.printState();
        UtpPacket packet =
            buildACKMessage(
                timeStamper.utpDifference(timeStamper.utpTimeStamp(), utpPacket.getTimestamp()),
                UtpAlgConfiguration.MAX_PACKET_SIZE * 1000L,
                timeStamper.utpTimeStamp(),
                this.session.getConnectionIdSending(),
                this.session.getAckNumber(),
                NO_EXTENSION);

        packet.setSequenceNumber(longToUshort(this.session.getSequenceNumber()));
        sendPacket(packet);
        connection.complete(null);
        this.session.printState();
      } catch (IOException exp) {
        // TODO:
        this.session.syncAckFailed();
        exp.printStackTrace();
      }
    } else {
      sendResetPacket();
    }
  }

  private void handleFinPacket(UtpPacket packet) {
    try {
      this.session.changeState(GOT_FIN);
      long freeBuffer =
          (reader.isPresent() && reader.get().isAlive())
              ? reader.get().getLeftSpaceInBuffer()
              : UtpAlgConfiguration.MAX_PACKET_SIZE;
      UtpPacket ackPacket =
          buildACKPacket(packet, timeStamper.utpDifference(packet.getTimestamp()), freeBuffer);
      sendPacket(ackPacket);
    } catch (IOException e) {
      // TODO error when sending ack
    }
  }

  private void handleConfirmationOfConnection(UtpPacket utpPacket) {
    if ((utpPacket.getConnectionId() & connectionIdMASK)
        == this.session.getConnectionIdReceiving()) {
      this.session.connectionConfirmed(utpPacket.getSequenceNumber());
      disableConnectionTimeOutCounter();
      connection.complete(null);
      this.session.printState();
    } else {
      sendResetPacket();
    }
  }

  private void disableConnectionTimeOutCounter() {
    if (retryConnectionTimeScheduler != null) {
      retryConnectionTimeScheduler.shutdown();
      retryConnectionTimeScheduler = null;
    }
    this.session.resetConnectionAttempts();
  }

  private void queuePacket(UtpPacket utpPacket) {
    if (utpPacket.getMessageType() == MessageType.ST_STATE
        && this.session.getState() == SessionState.SYN_SENT) {
      handleConfirmationOfConnection(utpPacket);
      return;
    }

    queue.offer(
        new UtpTimestampedPacketDTO(
            utpPacket, timeStamper.timeStamp(), timeStamper.utpTimeStamp()));
  }

  private void sendResetPacket() {
    // TODO
    LOG.debug("Sending RST packet MUST BE IMPLEMENTED");
  }

  public CompletableFuture<Void> write(ByteBuffer buffer) {
    return this.connection.thenCompose(
        v -> {
          this.writer = Optional.of(new UTPWritingFuture(this, buffer, timeStamper));
          return writer.get().startWriting();
        });
  }

  public CompletableFuture<Bytes> read() {
    return this.connection.thenCompose(
        v -> {
          this.reader = Optional.of(new UTPReadingFuture(this, timeStamper));
          return reader.get().startReading(this.session.getAckNumber()); // TODO FIX THIS
        });
  }

  public BlockingQueue<UtpTimestampedPacketDTO> getQueue() {
    return queue;
  }

  public void stop() {
    if (!listen.compareAndSet(true, false)) {
      LOG.warn("An attempt to stop an already stopping/stopped UTP server");
      return;
    }

    this.session.close();
    this.transportLayer.close(
        this.session.getConnectionIdReceiving(), this.session.getRemoteAddress());
    this.reader.ifPresent(UTPReadingFuture::graceFullInterrupt);
    this.writer.ifPresent(UTPWritingFuture::graceFullInterrupt);
  }

  public UtpPacket buildSelectiveACK(
      SelectiveAckHeaderExtension extension,
      int timestampDifference,
      long windowSize,
      byte firstExtension) {
    SelectiveAckHeaderExtension[] extensions = {extension};
    UtpPacket packet =
        buildACKMessage(
            timestampDifference,
            windowSize,
            timeStamper.utpTimeStamp(),
            this.session.getConnectionIdSending(),
            this.session.getAckNumber(),
            firstExtension);
    packet.setExtensions(extensions);
    return packet;
  }

  public UtpPacket buildDataPacket() {
    UtpPacket utpPacket =
        buildDataMessage(
            timeStamper.utpTimeStamp(),
            this.session.getConnectionIdSending(),
            this.session.getAckNumber(),
            this.session.getSequenceNumber());
    this.session.incrementeSeqNumber();
    return utpPacket;
  }

  public UtpPacket buildACKPacket(UtpPacket utpPacket, int timestampDifference, long windowSize)
      throws IOException {
    if (utpPacket.getTypeVersion() != FIN) {
      this.session.updateAckNumber(utpPacket.getSequenceNumber());
    }
    // TODO validate that the seq number is sent!
    return buildACKMessage(
        timestampDifference,
        windowSize,
        this.timeStamper.utpTimeStamp(),
        this.session.getConnectionIdSending(),
        this.getAckNumber(),
        NO_EXTENSION);
  }

  public void sendPacket(UtpPacket packet) throws IOException {
    this.transportLayer.sendPacket(packet, this.session.getRemoteAddress());
  }

  protected void startConnectionTimeOutCounter(UtpPacket synPacket) {
    retryConnectionTimeScheduler = Executors.newSingleThreadScheduledExecutor();
    retryConnectionTimeScheduler.scheduleWithFixedDelay(
        () -> {
          this.resendSynPacket(synPacket);
        },
        UtpAlgConfiguration.CONNECTION_ATTEMPT_INTERVALL_MILLIS,
        UtpAlgConfiguration.CONNECTION_ATTEMPT_INTERVALL_MILLIS,
        TimeUnit.MILLISECONDS);
  }

  public void resendSynPacket(UtpPacket synPacket) {
    int attempts = this.session.getConnectionAttempts();
    if (this.session.getState() != SessionState.SYN_SENT) {
      return;
    }
    if (attempts >= UtpAlgConfiguration.MAX_CONNECTION_ATTEMPTS) {
      this.connection.completeExceptionally(new SocketTimeoutException());
      retryConnectionTimeScheduler.shutdown();
      stop();
      return;
    }
    try {
      this.session.incrementeConnectionAttempts();
      this.transportLayer.sendPacket(synPacket, this.session.getRemoteAddress());
    } catch (IOException e) {
      this.connection.completeExceptionally(new SocketTimeoutException());
      retryConnectionTimeScheduler.shutdown();
      stop();
    }
  }

  public int getAckNumber() {
    return this.session.getAckNumber();
  }

  public int getSequenceNumber() {
    return this.session.getSequenceNumber();
  }

  public void setAckNumber(int ackNumber) {
    this.session.setAckNumer(ackNumber);
  }

  public void setTransportAddress(TransportAddress localhost) {
    this.session.setRemoteAddress(localhost);
  }

  public boolean isAlive() {
    return this.listen.get();
  }

  // for testing must be removed.
  public void setState(SessionState sessionState) {
    this.session.changeState(sessionState);
  }

  public void sendPacketFinPacket() {
    try {
      this.sendPacket(
          buildFINMessage(
              timeStamper.utpTimeStamp(),
              this.session.getConnectionIdSending(),
              this.session.getAckNumber(),
              this.session.getSequenceNumber()));
    } catch (Exception e) {
      LOG.debug("Error when sending Fin Packet");
    }
  }
}
