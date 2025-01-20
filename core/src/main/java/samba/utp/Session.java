package samba.utp;

import static samba.utp.SessionState.*;

import samba.utp.data.util.Utils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Session {

  private int MASK = 0xFFFF;
  private static int DEF_SEQ_START = 1;
  private static final Logger LOG = LogManager.getLogger(UTPClient.class);

  private long connectionIdSending;
  private long connectionIdReceiving;

  private SocketAddress transportAddress;
  private int ackNumber;
  private int sequenceNumber;
  private volatile SessionState state = null;

  private int connectionAttempts = 0;

  private final ReentrantLock lock = new ReentrantLock();

  public Session() {
    this.state = CLOSED;
  }

  public void initConnection(
      final SocketAddress transportAddress, final long connectionIdReceiving) {
    lock.lock();
    try {
      this.transportAddress = transportAddress;
      this.connectionIdReceiving = connectionIdReceiving;
      this.connectionIdSending = connectionIdReceiving + 1;
      this.sequenceNumber = DEF_SEQ_START;
    } finally {
      lock.unlock();
    }
  }

  public void initServerConnection(
      SocketAddress transportAddress, short connectionId, short sequenceNumber) {
    lock.lock();
    try {
      this.transportAddress = transportAddress;
      this.connectionIdSending = (connectionId & MASK);
      this.connectionIdReceiving = (connectionId & MASK) + 1;
      this.sequenceNumber = Utils.randomSeqNumber();
      this.ackNumber = sequenceNumber & MASK;
    } finally {
      lock.unlock();
    }
  }

  public void updateStateOnConnectionInitSuccess() {
    lock.lock();
    try {
      this.state = SYN_SENT;
      this.sequenceNumber = Utils.incrementSeqNumber(this.sequenceNumber);
    } finally {
      lock.unlock();
    }
  }

  public void printState() {
    String state =
        String.format(
            "[ConnID Sending: %d] [ConnID Recv: %d] [SeqNr. %d] [AckNr: %d] [State: %s]",
            connectionIdSending,
            connectionIdReceiving,
            sequenceNumber,
            ackNumber,
            this.state.name());
    LOG.info(state);
  }

  public void changeState(SessionState state) {
    lock.lock();
    try {
      this.state = state;
    } finally {
      lock.unlock();
    }
  }

  public long getConnectionIdReceiving() {
    return this.connectionIdReceiving;
  }

  public void connectionConfirmed(int ackNumber) {
    lock.lock();
    try {
      this.ackNumber = (ackNumber & MASK);
      this.state = CONNECTED;
    } finally {
      lock.unlock();
    }
  }

  public void close() {
    lock.lock();
    try {
      this.state = CLOSED;
      this.sequenceNumber = DEF_SEQ_START;
    } finally {
      lock.unlock();
    }
  }

  public void incrementeSeqNumber() {
    lock.lock();
    try {
      this.sequenceNumber = Utils.incrementSeqNumber(this.sequenceNumber);
    } finally {
      lock.unlock();
    }
  }

  public long getConnectionIdSending() {
    return this.connectionIdSending;
  }

  public int getAckNumber() {
    return this.ackNumber;
  }

  public int getSequenceNumber() {
    return this.sequenceNumber;
  }

  public void updateAckNumber(short sequenceNumber) {
    this.ackNumber = sequenceNumber & MASK;
  }

  public SocketAddress getTransportAddress() {
    return this.transportAddress;
  }

  public SessionState getState() {
    return this.state;
  }

  public int getConnectionAttempts() {
    return this.connectionAttempts;
  }

  public void incrementeConnectionAttempts() {
    lock.lock();
    try {
      this.connectionAttempts++;
    } finally {
      lock.unlock();
    }
  }

  public void syncAckFailed() {
    lock.lock();
    try {
      this.transportAddress = null;
      this.connectionIdSending = (short) 0;
      this.connectionIdReceiving = (short) 0;
      this.ackNumber = 0;
      this.state = SYN_ACKING_FAILED;
    } finally {
      lock.unlock();
    }
  }

  public void resetConnectionAttempts() {
    lock.lock();
    try {
      this.connectionAttempts = 0;
    } finally {
      lock.unlock();
    }
  }

  public void setAckNumer(int ackNumber) {
    lock.lock();
    try {
      this.ackNumber = ackNumber;
    } finally {
      lock.unlock();
    }
  }

  public void setTransportAddress(InetSocketAddress transportAddress) {
    lock.lock();
    try {
      this.transportAddress = transportAddress;
    } finally {
      lock.unlock();
    }
  }
}
