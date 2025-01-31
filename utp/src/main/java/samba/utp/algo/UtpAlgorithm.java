/* Copyright 2013 Ivan Iljkic
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package samba.utp.algo;

import static samba.utp.algo.UtpAlgConfiguration.*;

import samba.utp.UtpTimestampedPacketDTO;
import samba.utp.data.*;
import samba.utp.data.bytes.UnsignedTypesUtil;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtpAlgorithm {

  private int currentWindow = 0;
  private int maxWindow;
  private MinimumDelay minDelay = new MinimumDelay();
  private OutPacketBuffer buffer;
  private MicroSecondsTimeStamp timeStamper;
  private int currentAckPosition = 0;
  private int currentBurstSend = 0;
  private long lastZeroWindow;
  private ByteBuffer bBuffer;

  private long rtt;
  private long rttVar = 0;

  private int advertisedWindowSize;
  private boolean advertisedWindowSizeSet = false;

  private long lastTimeWindowReduced;
  private long timeStampNow;
  private long lastAckRecieved;

  private int resendedPackets = 0;
  private int totalPackets = 0;
  private long lastMaxedOutWindow;

  private static final Logger log = LoggerFactory.getLogger(UtpAlgorithm.class);

  public UtpAlgorithm(MicroSecondsTimeStamp timestamper) {
    maxWindow = MAX_CWND_INCREASE_PACKETS_PER_RTT;
    rtt = MINIMUM_TIMEOUT_MILLIS * 2L;
    timeStamper = timestamper;
    buffer = new OutPacketBuffer(timestamper);
    log.debug(UtpAlgConfiguration.getString());
    timeStampNow = timeStamper.timeStamp();
  }

  public void setOutPacketBuffer(OutPacketBuffer outBuffer) {
    this.buffer = outBuffer;
  }

  /**
   * handles the acking of the packet.
   *
   * @param pair packet with the meta data.
   */
  public void ackRecieved(UtpTimestampedPacketDTO pair) {
    int seqNrToAck = pair.utpPacket().getAckNumber() & 0xFFFF;
    //		log.debug("Recieved ACK " + pair.utpPacket().toString());
    timeStampNow = timeStamper.timeStamp();
    lastAckRecieved = timeStampNow;
    int advertisedWindo = pair.utpPacket().getWindowSize() & 0xFFFFFFFF;
    updateAdvertisedWindowSize(advertisedWindo);
    int packetSizeJustAcked =
        buffer.markPacketAcked(
            seqNrToAck, timeStampNow, UtpAlgConfiguration.AUTO_ACK_SMALLER_THAN_ACK_NUMBER);
    if (packetSizeJustAcked > 0) {
      updateRtt(timeStampNow, seqNrToAck);
      updateWindow(pair.utpPacket(), timeStampNow, packetSizeJustAcked, pair.utpTimeStamp());
    }
    // TODO: With libutp, sometimes null pointer exception -> investigate.
    //			log.debug("utpPacket With Ext: " + pair.utpPacket().toString());
    SelectiveAckHeaderExtension selectiveAckExtension = findSelectiveAckExtension(pair.utpPacket());
    if (selectiveAckExtension != null) {

      // if a new packed is acked by selectiveAck, we will
      // only update this one. if more than one is acked newly,
      // ignore it, because it will corrupt our measurements
      boolean windowAlreadyUpdated = false;

      // For each byte in the selective Ack header extension
      byte[] bitMask = selectiveAckExtension.getBitMask();
      for (int i = 0; i < bitMask.length; i++) {
        // each bit in the extension, from 2 to 9, because least significant
        // bit is ACK+2, most significant bit is ack+9 -> loop [2,9]
        for (int j = 2; j < 10; j++) {
          if (SelectiveAckHeaderExtension.isBitMarked(bitMask[i], j)) {
            // j-th bit of i-th byte + seqNrToAck equals our selective-Ack-number.
            // example:
            // ack:5, sack: 8 -> i = 0, j =3 -> 0*8+3+5 = 8.
            // bitpattern in this case would be 00000010, bit_index 1 from right side, added 2 to it
            // equals 3
            // thats why we start with j=2. most significant bit is index 7, j would be 9 then.
            int sackSeqNr = i * 8 + j + seqNrToAck;
            // sackSeqNr can overflow too !!
            if (sackSeqNr > UnsignedTypesUtil.MAX_USHORT) {
              sackSeqNr -= (int) UnsignedTypesUtil.MAX_USHORT;
            }
            // dont ack smaller seq numbers in case of Selective ack !!!!!
            packetSizeJustAcked = buffer.markPacketAcked(sackSeqNr, timeStampNow, false);
            if (packetSizeJustAcked > 0 && !windowAlreadyUpdated) {
              windowAlreadyUpdated = true;
              updateRtt(timeStampNow, sackSeqNr);
              updateWindow(
                  pair.utpPacket(), timeStampNow, packetSizeJustAcked, pair.utpTimeStamp());
            }
          }
        }
      }
    }
  }

  private void updateRtt(long timestamp, int seqNrToAck) {
    long sendTimeStamp = buffer.getSendTimeStamp(seqNrToAck);
    if (rttUpdateNecessary(sendTimeStamp, seqNrToAck)) {
      long packetRtt = (timestamp - sendTimeStamp) / 1000;
      long delta = rtt - packetRtt;
      rttVar += (Math.abs(delta) - rttVar) / 4;
      rtt += (packetRtt - rtt) / 8;
    }
  }

  private boolean rttUpdateNecessary(long sendTimeStamp, int seqNrToAck) {
    return sendTimeStamp != -1 && buffer.getResendCounter(seqNrToAck) == 0;
  }

  private void updateAdvertisedWindowSize(int advertisedWindo) {
    if (!advertisedWindowSizeSet) {
      advertisedWindowSizeSet = true;
    }
    this.advertisedWindowSize = advertisedWindo;
  }

  private void updateWindow(
      UtpPacket utpPacket, long timestamp, int packetSizeJustAcked, int utpRecieved) {
    currentWindow = buffer.getBytesOnfly();

    if (isWondowFull()) {
      lastMaxedOutWindow = timeStampNow;
    }
    long ourDifference = utpPacket.getTimestampDifference() & 0xFFFFFFFF;
    updateOurDelay(ourDifference);

    int theirDifference = timeStamper.utpDifference(utpRecieved, utpPacket.getTimestamp());
    updateTheirDelay(theirDifference);

    long ourDelay = ourDifference - minDelay.getCorrectedMinDelay();
    minDelay.addSample(ourDelay);

    long offTarget = C_CONTROL_TARGET_MICROS - ourDelay;
    double delayFactor = ((double) offTarget) / ((double) C_CONTROL_TARGET_MICROS);
    double windowFactor =
        (Math.min(packetSizeJustAcked, (double) maxWindow))
            / (Math.max(maxWindow, (double) packetSizeJustAcked));
    int gain = (int) (MAX_CWND_INCREASE_PACKETS_PER_RTT * delayFactor * windowFactor);

    if (setGainToZero(gain)) {
      gain = 0;
    }
    maxWindow += gain;
    if (maxWindow < 0) {
      maxWindow = 0;
    }

    buffer.setResendtimeOutMicros(getTimeOutMicros());

    if (maxWindow == 0) {
      lastZeroWindow = timeStampNow;
    }
    // get bytes successfully transmitted:
    // this is the position of the bytebuffer (comes from programmer)
    // substracted by the amount of bytes on fly (these are not yet acked)
    int bytesSend = bBuffer.position() - buffer.getBytesOnfly();

    //		maxWindow = 10000;
  }

  private boolean setGainToZero(int gain) {
    // if i have ever reached lastMaxWindow then check if its longer than 1kk micros
    // if not, true
    boolean lastMaxWindowNeverReached =
        lastMaxedOutWindow == 0
            || (lastMaxedOutWindow - timeStampNow
                >= UtpAlgConfiguration.MINIMUM_DELTA_TO_MAX_WINDOW_MICROS);
    if (lastMaxWindowNeverReached) {
      log.debug("last maxed window: setting gain to 0");
    }
    return (ONLY_POSITIVE_GAIN && gain < 0) || lastMaxWindowNeverReached;
  }

  private void updateTheirDelay(long theirDifference) {
    minDelay.updateTheirDelay(theirDifference, timeStampNow);
  }

  private long getTimeOutMicros() {
    return Math.max(getEstimatedRttMicros(), MINIMUM_TIMEOUT_MILLIS * 1000L);
  }

  private long getEstimatedRttMicros() {
    return rtt * 1000 + rttVar * 4 * 1000;
  }

  private void updateOurDelay(long difference) {
    minDelay.updateOurDelay(difference, timeStampNow);
  }

  /**
   * Checks if packets must be resend based on the fast resend mechanism or a transmission timeout.
   *
   * @return All packets that must be resend
   */
  public Queue<UtpPacket> getPacketsToResend() throws SocketException {
    timeStampNow = timeStamper.timeStamp();
    Queue<UtpPacket> queue = new LinkedList<>();

    Queue<UtpTimestampedPacketDTO> toResend =
        buffer.getPacketsToResend(UtpAlgConfiguration.MAX_BURST_SEND);

    for (UtpTimestampedPacketDTO utpTimestampedPacketDTO : toResend) {
      queue.add(utpTimestampedPacketDTO.utpPacket());
      utpTimestampedPacketDTO.incrementResendCounter();
      if (utpTimestampedPacketDTO.reduceWindow()) {
        if (reduceWindowNecessary()) {
          lastTimeWindowReduced = timeStampNow;
          maxWindow /= 2;
        }
        utpTimestampedPacketDTO.setReduceWindow(false);
      }
    }
    resendedPackets += queue.size();
    return queue;
  }

  private boolean reduceWindowNecessary() {
    if (lastTimeWindowReduced == 0) {
      return true;
    }

    long delta = timeStampNow - lastTimeWindowReduced;
    return delta > getEstimatedRttMicros();
  }

  private SelectiveAckHeaderExtension findSelectiveAckExtension(UtpPacket utpPacket) {
    UtpHeaderExtension[] extensions = utpPacket.getExtensions();
    if (extensions == null) {
      return null;
    }
    for (UtpHeaderExtension extension : extensions) {
      if (extension instanceof SelectiveAckHeaderExtension) {
        return (SelectiveAckHeaderExtension) extension;
      }
    }
    return null;
  }

  /** Returns true if a packet can NOW be send */
  public boolean canSendNextPacket() {
    if (timeStampNow - lastZeroWindow > getTimeOutMicros()
        && lastZeroWindow != 0
        && maxWindow == 0) {
      log.debug("setting window to one packet size. current window is:" + currentWindow);
      maxWindow = MAX_PACKET_SIZE;
    }
    boolean windowNotFull = !isWondowFull();
    boolean burstFull = false;

    if (windowNotFull) {
      burstFull = isBurstFull();
    }

    if (!burstFull && windowNotFull) {
      currentBurstSend++;
    }

    if (burstFull) {
      currentBurstSend = 0;
    }
    return SEND_IN_BURST ? (!burstFull && windowNotFull) : windowNotFull;
  }

  private boolean isBurstFull() {
    return currentBurstSend >= MAX_BURST_SEND;
  }

  private boolean isWondowFull() {
    int maximumWindow =
        (advertisedWindowSize < maxWindow && advertisedWindowSizeSet)
            ? advertisedWindowSize
            : maxWindow;
    return currentWindow >= maximumWindow;
  }

  /**
   * Returns the size of the next packet, depending on {@see PacketSizeModus}
   *
   * @return bytes.
   */
  public int sizeOfNextPacket() {
    if (PACKET_SIZE_MODE.equals(PacketSizeModus.DYNAMIC_LINEAR)) {
      return calculateDynamicLinearPacketSize();
    } else if (PACKET_SIZE_MODE.equals(PacketSizeModus.CONSTANT_1472)) {
      return MAX_PACKET_SIZE - UtpPacketUtils.DEF_HEADER_LENGTH;
    }
    return MINIMUM_MTU - UtpPacketUtils.DEF_HEADER_LENGTH;
  }

  private int calculateDynamicLinearPacketSize() {
    int packetSizeDelta = MAX_PACKET_SIZE - MIN_PACKET_SIZE;
    long minDelayOffTarget = C_CONTROL_TARGET_MICROS - minDelay.getRecentAverageDelay();
    minDelayOffTarget = minDelayOffTarget < 0 ? 0 : minDelayOffTarget;
    double packetSizeFactor = ((double) minDelayOffTarget) / ((double) C_CONTROL_TARGET_MICROS);
    double packetSize = MIN_PACKET_SIZE + packetSizeFactor * packetSizeDelta;
    return (int) Math.ceil(packetSize);
  }

  /**
   * Inform the algorithm that this packet just was send
   *
   * @param utpPacket utp packet version
   */
  public void markPacketOnfly(UtpPacket utpPacket) {
    timeStampNow = timeStamper.timeStamp();
    UtpTimestampedPacketDTO pkt = new UtpTimestampedPacketDTO(utpPacket, timeStampNow, 0);
    buffer.bufferPacket(pkt);
    incrementAckNumber();
    addPacketToCurrentWindow(utpPacket);
    totalPackets++;
  }

  private void incrementAckNumber() {
    if (currentAckPosition == UnsignedTypesUtil.MAX_USHORT) {
      currentAckPosition = 1;
    } else {
      currentAckPosition++;
    }
  }

  /** informs the algorithm that the fin packet was send. */
  public void markFinOnfly(UtpPacket fin) {
    timeStampNow = timeStamper.timeStamp();
    byte[] finBytes = fin.toByteArray();
    UtpTimestampedPacketDTO pkt = new UtpTimestampedPacketDTO(fin, timeStampNow, 0);
    buffer.bufferPacket(pkt);
    incrementAckNumber();
    addPacketToCurrentWindow(fin);
  }

  private void addPacketToCurrentWindow(UtpPacket pkt) {
    currentWindow += UtpPacketUtils.DEF_HEADER_LENGTH;
    if (pkt.getPayload() != null) {
      currentWindow += pkt.getPayload().length;
    }
  }

  public boolean areAllPacketsAcked() {
    return buffer.isEmpty();
  }

  public MinimumDelay getMinDelay() {
    return minDelay;
  }

  public void setMinDelay(MinimumDelay minDelay) {
    this.minDelay = minDelay;
  }

  public void setTimeStamper(MicroSecondsTimeStamp timeStamper) {
    this.timeStamper = timeStamper;
  }

  /** sets the current ack position based on the sequence number */
  public void initiateAckPosition(int sequenceNumber) {
    if (sequenceNumber == 0) {
      throw new IllegalArgumentException("sequence number cannot be 0");
    }
    if (sequenceNumber == 1) {
      currentAckPosition = (int) UnsignedTypesUtil.MAX_USHORT;
    } else {
      currentAckPosition = sequenceNumber - 1;
    }
  }

  /**
   * Helper. Returns a String of the binary representation of the given value.
   *
   * @param value to convert the value
   * @return String binary representation.
   */
  private static String toBinaryString(int value, int length) {
    String result = Integer.toBinaryString(value);

    StringBuilder buf = new StringBuilder();
    for (int i = 0; (i + result.length()) < length; i++) {
      buf.append('0');
    }
    buf.append(result);
    return buf.toString();
  }

  /**
   * Helper. Returns a String of the binary representation of the given value.
   *
   * @param value to convert the value
   * @return String binary representation.
   */
  public static String toBinaryString(byte value) {
    return toBinaryString((value & 0xFF), 8);
  }

  public void removeAcked() {
    buffer.removeAcked();
    currentWindow = buffer.getBytesOnfly();
  }

  public String getLeftElements() {
    return buffer.getSequenceOfLeft();
  }

  /**
   * Returns the number of micro seconds the writing thread should wait at most based on: timed out
   * packets and window utilisation
   *
   * @return micro seconds.
   */
  public long getWaitingTimeMicroSeconds() {
    long oldestTimeStamp = buffer.getOldestUnackedTimestamp();
    long nextTimeOut = oldestTimeStamp + getTimeOutMicros();
    timeStampNow = timeStamper.timeStamp();
    long timeOutInMicroSeconds = nextTimeOut - timeStampNow;
    if (continueImmidiately(timeOutInMicroSeconds, oldestTimeStamp)) {
      return 0L;
    }
    if (!isWondowFull() || maxWindow == 0) {
      return MICROSECOND_WAIT_BETWEEN_BURSTS;
    }
    return timeOutInMicroSeconds;
  }

  private boolean continueImmidiately(long timeOutInMicroSeconds, long oldestTimeStamp) {
    return timeOutInMicroSeconds < 0 && (oldestTimeStamp != 0);
  }

  /**
   * terminates.
   *
   * @param bytesSend
   * @param successfull
   */
  public void end(int bytesSend, boolean successfull) {
    if (successfull) {
      log.debug(
          "Total packets send: " + totalPackets + ", Total Packets Resend: " + resendedPackets);
    }
  }

  public void resetBurst() {
    currentBurstSend = 0;
  }

  /** returns true when a socket timeout happened. (the reciever does not answer anymore) */
  public boolean isTimedOut() {
    if (timeStampNow - lastAckRecieved > getTimeOutMicros() * 5 && lastAckRecieved != 0) {
      log.debug("Timed out!");
      return true;
    }
    return false;
  }

  public void setMaxWindow(int window) {
    this.maxWindow = window;
  }

  public int getMaxWindow() {
    return maxWindow;
  }

  public int getCurrentWindow() {
    return currentWindow;
  }

  public void setByteBuffer(ByteBuffer bBuffer) {
    this.bBuffer = bBuffer;
  }

  public void setCurrentWindow(int i) {
    this.currentWindow = i;
  }

  public void setEstimatedRtt(int i) {
    this.rtt = i;
  }
}
