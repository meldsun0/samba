package samba.utp.operations;

import static samba.utp.data.bytes.UnsignedTypesUtil.MAX_USHORT;

import samba.utp.UtpTimestampedPacketDTO;
import samba.utp.data.SelectiveAckHeaderExtension;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkippedPacketBuffer {

  private static final int SIZE = 2000;
  private final UtpTimestampedPacketDTO[] buffer = new UtpTimestampedPacketDTO[SIZE];
  private final AtomicInteger expectedSequenceNumber = new AtomicInteger(0);
  private final AtomicInteger elementCount = new AtomicInteger(0);

  private static final Logger log = LoggerFactory.getLogger(SkippedPacketBuffer.class);

  private final Lock lock = new ReentrantLock(); // Fine-grained locking

  /**
   * Puts the packet in the buffer.
   *
   * @param pkt the packet with meta data.
   * @throws IOException
   */
  public void bufferPacket(UtpTimestampedPacketDTO pkt) throws IOException {
    lock.lock();
    try {
      int sequenceNumber = pkt.utpPacket().getSequenceNumber() & 0xFFFF;
      int position = sequenceNumber - expectedSequenceNumber.get();
      if (position < 0) {
        position = mapOverflowPosition(sequenceNumber);
      }

      elementCount.incrementAndGet();

      try {
        buffer[position] = pkt;
      } catch (ArrayIndexOutOfBoundsException ioobe) {
        log.info("seq, exp: " + sequenceNumber + " " + expectedSequenceNumber + " ");
        ioobe.printStackTrace();
        throw new IOException();
      }
    } finally {
      lock.unlock();
    }
  }

  private int mapOverflowPosition(int sequenceNumber) {
    return (int) (MAX_USHORT - expectedSequenceNumber.get() + sequenceNumber);
  }

  public void setExpectedSequenceNumber(int seq) {
    expectedSequenceNumber.set(seq);
  }

  public int getExpectedSequenceNumber() {
    return expectedSequenceNumber.get();
  }

  public SelectiveAckHeaderExtension createHeaderExtension() {
    SelectiveAckHeaderExtension header = new SelectiveAckHeaderExtension();
    int length = calculateHeaderLength();
    byte[] bitMask = new byte[length];
    fillBitMask(bitMask);
    header.setBitMask(bitMask);
    return header;
  }

  private void fillBitMask(byte[] bitMask) {
    int bitMaskIndex = 0;
    for (int i = 1; i < SIZE; i++) {
      int bitMapIndex = (i - 1) % 8;
      boolean hasReceived = buffer[i] != null;

      if (hasReceived) {
        int bitPattern = (SelectiveAckHeaderExtension.BITMAP[bitMapIndex] & 0xFF) & 0xFF;
        bitMask[bitMaskIndex] = (byte) ((bitMask[bitMaskIndex] & 0xFF) | bitPattern);
      }

      if (i % 8 == 0) {
        bitMaskIndex++;
      }
    }
  }

  private int calculateHeaderLength() {
    int size = getRange();
    return (((size - 1) / 32) + 1) * 4;
  }

  private int getRange() {
    int range = 0;
    for (int i = 0; i < SIZE; i++) {
      if (buffer[i] != null) {
        range = i;
      }
    }
    return range;
  }

  public boolean isEmpty() {
    return elementCount.get() == 0;
  }

  public Queue<UtpTimestampedPacketDTO> getAllUntilNextMissing() {
    lock.lock();
    try {
      Queue<UtpTimestampedPacketDTO> queue = new LinkedList<>();
      for (int i = 1; i < SIZE; i++) {
        if (buffer[i] != null) {
          queue.add(buffer[i]);
          buffer[i] = null;
        } else {
          break;
        }
      }
      elementCount.addAndGet(-queue.size());
      return queue;
    } finally {
      lock.unlock();
    }
  }

  public void reindex(int lastSeqNumber) throws IOException {
    lock.lock();
    try {
      int newExpectedSequenceNumber = (lastSeqNumber == MAX_USHORT) ? 1 : lastSeqNumber + 1;
      setExpectedSequenceNumber(newExpectedSequenceNumber);

      UtpTimestampedPacketDTO[] oldBuffer =
          buffer.clone(); // Use a clone to avoid holding the lock while accessing buffer
      resetBuffer();

      for (UtpTimestampedPacketDTO utpTimestampedPacket : oldBuffer) {
        if (utpTimestampedPacket != null) {
          bufferPacket(utpTimestampedPacket);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private void resetBuffer() {
    for (int i = 0; i < SIZE; i++) {
      buffer[i] = null;
    }
    elementCount.set(0);
  }

  public int getFreeSize() throws IOException {
    lock.lock();
    try {
      if (SIZE - elementCount.get() < 0) {}
      if (SIZE - elementCount.get() < 50) {
        return 0;
      }
      return SIZE - elementCount.get() - 1;
    } finally {
      lock.unlock();
    }
  }
}
