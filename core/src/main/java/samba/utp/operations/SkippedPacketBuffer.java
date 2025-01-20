package samba.utp.operations;

import static samba.utp.data.bytes.UnsignedTypesUtil.MAX_USHORT;

import samba.utp.UtpTimestampedPacketDTO;
import samba.utp.algo.UtpAlgConfiguration;
import samba.utp.data.SelectiveAckHeaderExtension;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
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
  private int debug_lastSeqNumber;
  private int debug_lastPosition;

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
      debug_lastSeqNumber = sequenceNumber;

      if (position < 0) {
        position = mapOverflowPosition(sequenceNumber);
      }

      debug_lastPosition = position;
      elementCount.incrementAndGet();

      try {
        buffer[position] = pkt;
      } catch (ArrayIndexOutOfBoundsException ioobe) {
        log.error("seq, exp: " + sequenceNumber + " " + expectedSequenceNumber + " ");
        ioobe.printStackTrace();
        dumpBuffer("oob: " + ioobe.getMessage());
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
      if (SIZE - elementCount.get() < 0) {
        dumpBuffer("freesize negative");
      }
      if (SIZE - elementCount.get() < 50) {
        return 0;
      }
      return SIZE - elementCount.get() - 1;
    } finally {
      lock.unlock();
    }
  }

  private void dumpBuffer(String message) throws IOException {
    if (UtpAlgConfiguration.DEBUG) {
      log.debug("dumping buffer");
      try (RandomAccessFile aFile = new RandomAccessFile("testData/auto/bufferdump.txt", "rw");
          FileChannel inChannel = aFile.getChannel()) {

        inChannel.truncate(0);
        ByteBuffer bbuffer = ByteBuffer.allocate(100000);
        bbuffer.put((new SimpleDateFormat("dd_MM_hh_mm_ss")).format(new Date()).getBytes());
        bbuffer.put((message + "\n").getBytes());
        bbuffer.put(("SIZE: " + SIZE + "\n").getBytes());
        bbuffer.put(("count: " + elementCount.get() + "\n").getBytes());
        bbuffer.put(("expect: " + expectedSequenceNumber.get() + "\n").getBytes());
        bbuffer.put(("lastSeq: " + debug_lastSeqNumber + "\n").getBytes());
        bbuffer.put(("lastPos: " + debug_lastPosition + "\n").getBytes());

        for (int i = 0; i < SIZE; i++) {
          String seq =
              (buffer[i] == null)
                  ? "_; "
                  : (buffer[i].utpPacket().getSequenceNumber() & 0xFFFF) + "; ";
          bbuffer.put((i + " -> " + seq).getBytes());
          if (i % 50 == 0) {
            bbuffer.put("\n".getBytes());
          }
        }
        log.debug(bbuffer.position() + " " + bbuffer.limit());
        bbuffer.flip();
        while (bbuffer.hasRemaining()) {
          inChannel.write(bbuffer);
        }
      }
    }
  }
}
