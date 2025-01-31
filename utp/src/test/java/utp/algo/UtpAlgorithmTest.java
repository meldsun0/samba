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
package utp.algo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;

import samba.utp.UtpTimestampedPacketDTO;
import samba.utp.algo.*;
import samba.utp.data.MicroSecondsTimeStamp;
import samba.utp.data.SelectiveAckHeaderExtension;
import samba.utp.data.UtpPacket;
import samba.utp.data.UtpPacketUtils;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Queue;

import org.junit.jupiter.api.Test;

public class UtpAlgorithmTest {

  /** Packet size testing, dynamic, linear */
  @Test
  public void testDynamicPacketSize() {
    int c_target = 100000;
    UtpAlgConfiguration.PACKET_SIZE_MODE = PacketSizeModus.DYNAMIC_LINEAR;
    UtpAlgConfiguration.C_CONTROL_TARGET_MICROS = c_target;
    MinimumDelay mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn((long) c_target / 2);

    /* when delay is 50ms, then packetsize =
     * (delay/max_delay)*(max_packet-min_packet)+min_packet */
    MicroSecondsTimeStamp stamper = new MicroSecondsTimeStamp();
    UtpAlgorithm alg = new UtpAlgorithm(stamper);
    alg.setMinDelay(mockMinDelay);
    assertEquals(811, alg.sizeOfNextPacket());

    mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn((long) c_target);
    alg.setMinDelay(mockMinDelay);
    assertEquals(150, alg.sizeOfNextPacket());

    mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn(0L);
    alg.setMinDelay(mockMinDelay);
    assertEquals(1472, alg.sizeOfNextPacket());

    mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn((long) c_target / 10 * 3);
    alg.setMinDelay(mockMinDelay);
    assertEquals(1076, alg.sizeOfNextPacket());

    mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn((long) c_target / 10 * 7);
    alg.setMinDelay(mockMinDelay);
    assertEquals(547, alg.sizeOfNextPacket());

    mockMinDelay = mock(MinimumDelay.class);
    when(mockMinDelay.getRecentAverageDelay()).thenReturn((long) c_target / 10 * 12);
    alg.setMinDelay(mockMinDelay);
    assertEquals(150, alg.sizeOfNextPacket());
  }

  @Test
  public void testAcking() throws SocketException {
    UtpAlgConfiguration.AUTO_ACK_SMALLER_THAN_ACK_NUMBER = true;
    UtpAlgConfiguration.MIN_SKIP_PACKET_BEFORE_RESEND = 3;

    MicroSecondsTimeStamp stamper = mock(MicroSecondsTimeStamp.class);
    when(stamper.timeStamp()).thenReturn(0L);

    UtpAlgorithm algorithm = new UtpAlgorithm(stamper);
    ByteBuffer bufferMock = ByteBuffer.allocate(10);
    bufferMock.put(0, (byte) 42);
    algorithm.setByteBuffer(bufferMock);

    // Add some packets, 4...14
    UtpTimestampedPacketDTO pkt = createPacket(3);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(4);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(5);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(6);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(7);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(8);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(9);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(10);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(11);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(12);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(13);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(14);
    algorithm.markPacketOnfly(pkt.utpPacket());

    // now 11 unacked packets: 3,...,13,14
    // ack with following: ACK:5, SACK: 7,8,9,10,11,12,13,14 -> should trigger resend 6
    // because 3,4,5 is beeing autoacked, 7,8,9,10,11,12,13,14 beeing acked by selective ack.
    // ACK:5,SACK:7,8,9,10,11,12,13,14 bitpattern: 11111111 -> least significant bit is always ACK+2
    // in this case its 7.
    byte[] selAck = {(byte) 255, (byte) 0, (byte) 0, (byte) 0};
    UtpTimestampedPacketDTO ackPacket = createSelAckPacket(5, selAck);
    algorithm.ackRecieved(ackPacket);

    // now 3,4,5 should be removed
    algorithm.removeAcked();
    String leftElements = algorithm.getLeftElements();
    assertEquals("6 7 8 9 10 11 12 13 14", leftElements);

    // 3 past 6 are acked, trigger an resend of 6
    Queue<UtpPacket> packetsToResend = algorithm.getPacketsToResend();
    assertEquals(1, packetsToResend.size());
    assertEquals(6, (packetsToResend.remove()).getSequenceNumber() & 0xFFFF);

    // 6 beeing acked now.
    UtpTimestampedPacketDTO ack6 = createSelAckPacket(6, null);
    // no extension...
    ack6.utpPacket().setFirstExtension((byte) 0);
    ack6.utpPacket().setExtensions(null);

    algorithm.ackRecieved(ack6);
    algorithm.removeAcked();

    // everything is now acked
    leftElements = algorithm.getLeftElements();
    assertEquals("", leftElements);

    // no packets to resend
    packetsToResend = algorithm.getPacketsToResend();
    assertEquals(0, packetsToResend.size());
  }

  private UtpTimestampedPacketDTO createSelAckPacket(int i, byte[] selAck) throws SocketException {
    UtpTimestampedPacketDTO ack = createPacket(2);
    ack.utpPacket().setAckNumber((short) (i & 0xFFFF));

    SelectiveAckHeaderExtension selAckExtension = new SelectiveAckHeaderExtension();
    selAckExtension.setBitMask(selAck);
    selAckExtension.setNextExtension((byte) 0);
    ack.utpPacket().setFirstExtension(UtpPacketUtils.SELECTIVE_ACK);

    SelectiveAckHeaderExtension[] extensions = {selAckExtension};
    ack.utpPacket().setExtensions(extensions);

    return ack;
  }

  @Test
  public void testResendNoTriggerReduceWindow() throws SocketException {
    UtpAlgConfiguration.AUTO_ACK_SMALLER_THAN_ACK_NUMBER = true;
    UtpAlgConfiguration.MIN_SKIP_PACKET_BEFORE_RESEND = 3;

    int maxWindow = 100000;

    MicroSecondsTimeStamp stamper = mock(MicroSecondsTimeStamp.class);
    when(stamper.timeStamp()).thenReturn(0L);

    UtpAlgorithm algorithm = new UtpAlgorithm(stamper);
    ByteBuffer bufferMock = ByteBuffer.allocate(10);
    bufferMock.put(0, (byte) 42);
    algorithm.setByteBuffer(bufferMock);

    UtpTimestampedPacketDTO pkt = createPacket(5);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(6);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(7);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(8);
    algorithm.markPacketOnfly(pkt.utpPacket());
    pkt = createPacket(9);
    algorithm.markPacketOnfly(pkt.utpPacket());

    // packets 5,6,7,8,9 on fly
    // now will ACK:5, SACK: 7,8,9
    // SACK bits as follow: 00000111 000... ... ...000
    // this means, 3 packets past 6 have been acked.
    // should trigger fast resend of 6.
    // maxWindow should not be multiplied with 0.5.
    byte[] selAck = {(byte) 7, 0, 0, 0};
    UtpTimestampedPacketDTO sack = createSelAckPacket(5, selAck);
    algorithm.ackRecieved(sack);
    algorithm.setMaxWindow(maxWindow);

    Queue<UtpPacket> queue = algorithm.getPacketsToResend();
    assertEquals(1, queue.size());

    assertEquals(maxWindow, algorithm.getMaxWindow());
  }

  @Test
  public void testPacketSending() throws SocketException {
    MicroSecondsTimeStamp stamper = mock(MicroSecondsTimeStamp.class);
    when(stamper.timeStamp()).thenReturn(0L);
    UtpAlgorithm algorithm = new UtpAlgorithm(stamper);
    UtpAlgConfiguration.SEND_IN_BURST = true;
    UtpAlgConfiguration.MAX_BURST_SEND = 3;

    int packetLength = 1000;

    // make room for 10 packets.
    algorithm.setMaxWindow(packetLength * 10);

    // mark 5 packets on fly, will be ~5100 bytes of currentWindow
    UtpTimestampedPacketDTO pkt5 = createPacket(5, packetLength);
    UtpTimestampedPacketDTO pkt6 = createPacket(6, packetLength);
    UtpTimestampedPacketDTO pkt7 = createPacket(7, packetLength);
    UtpTimestampedPacketDTO pkt8 = createPacket(8, packetLength);
    UtpTimestampedPacketDTO pkt9 = createPacket(9, packetLength);

    algorithm.markPacketOnfly(pkt5.utpPacket());
    algorithm.markPacketOnfly(pkt6.utpPacket());
    algorithm.markPacketOnfly(pkt7.utpPacket());
    algorithm.markPacketOnfly(pkt8.utpPacket());
    algorithm.markPacketOnfly(pkt9.utpPacket());

    assertEquals(
        5 * (UtpPacketUtils.DEF_HEADER_LENGTH + packetLength), algorithm.getCurrentWindow());

    // our current window is smaller than max window. MAX_BURST_SEND times invocating should trigger
    // a true
    for (int i = 0; i < UtpAlgConfiguration.MAX_BURST_SEND; i++) {
      assertTrue(algorithm.canSendNextPacket());
    }

    // now we cannot send a packet anymore
    assertFalse(algorithm.canSendNextPacket());

    // now again we can send 3 packets...
    for (int i = 0; i < UtpAlgConfiguration.MAX_BURST_SEND; i++) {
      assertTrue(algorithm.canSendNextPacket());
    }
    // and now false.
    assertFalse(algorithm.canSendNextPacket());

    // lets reduce maxwindow to 4* packet length, no packets can be send now.
    algorithm.setMaxWindow(packetLength * 4);
    for (int i = 0; i < UtpAlgConfiguration.MAX_BURST_SEND; i++) {
      assertFalse(algorithm.canSendNextPacket());
    }

    // we still cannot send packets...
    for (int i = 0; i < UtpAlgConfiguration.MAX_BURST_SEND; i++) {
      assertFalse(algorithm.canSendNextPacket());
    }

    // increase max window again.
    algorithm.setMaxWindow(10 * packetLength);

    // send 3 packets in one burst.
    for (int i = 0; i < UtpAlgConfiguration.MAX_BURST_SEND; i++) {
      assertTrue(algorithm.canSendNextPacket());
    }

    // current burst full, next invocation should be false.
    assertFalse(algorithm.canSendNextPacket());
  }

  @Test
  public void testWaitingTime() {
    /*Behaviour should be:
     *
     * timeout time when next packet will timeout, but only if window is full. else waiting time micros.
     * immidiately when packet has timed out. else waiting time micros
     * when buffer empty, maxWindow 0 => waiting time
     * when buffer non empty, maxwindow = 0; nontimeout => waiting time.
     *  when buffer non empty, maxwindow = 0; timedout = immidiately
     */
    UtpAlgConfiguration.MINIMUM_TIMEOUT_MILLIS = 500;
    MicroSecondsTimeStamp stamper = mock(MicroSecondsTimeStamp.class);
    when(stamper.timeStamp()).thenReturn(1000000L); // returns 1s
    UtpAlgorithm algorithm = new UtpAlgorithm(stamper);
    algorithm.setEstimatedRtt(0);

    OutPacketBuffer outBuffer = mock(OutPacketBuffer.class);
    when(outBuffer.getOldestUnackedTimestamp()).thenReturn(600000L);
    //		when(outBuffer.getBytesOnfly()).thenReturn(20000); // 20 kB onfly
    algorithm.setCurrentWindow(20000);
    algorithm.setOutPacketBuffer(outBuffer);

    algorithm.setMaxWindow(20000); // maxWindow 20kB -> window is full.

    // Situation: window full. waiting time should be to next timeout:
    // since rtt and rtt_var is 0: timeout delta is 500'000:
    // oldest + 500k = 1'100'000 is the time when next timeout happens.
    long waitingTime = algorithm.getWaitingTimeMicroSeconds();

    assertEquals(100000, waitingTime);

    // now current window is below 20k.
    //		when(outBuffer.getBytesOnfly()).thenReturn(10000); // 20 kB onfly
    algorithm.setCurrentWindow(10000);

    waitingTime = algorithm.getWaitingTimeMicroSeconds();
    assertEquals(UtpAlgConfiguration.MICROSECOND_WAIT_BETWEEN_BURSTS, waitingTime);

    // now test when a packet has timed out.

    when(outBuffer.getOldestUnackedTimestamp()).thenReturn(10000L);
    when(stamper.timeStamp()).thenReturn(700000L);
    // oldest is 10'000, timeout is in 10'000 + 500'000 = 510'000. now is 700'000 => timeout.
    waitingTime = algorithm.getWaitingTimeMicroSeconds();
    assertEquals(0, waitingTime);

    // current window 20k, nonempty, nontimeout but down to max_window 0. should keep waiting
    // normally.
    algorithm.setCurrentWindow(20000);
    algorithm.setMaxWindow(0);
    when(outBuffer.getOldestUnackedTimestamp()).thenReturn(100000L);
    when(stamper.timeStamp()).thenReturn(500000L);
    waitingTime = algorithm.getWaitingTimeMicroSeconds();
    assertEquals(UtpAlgConfiguration.MICROSECOND_WAIT_BETWEEN_BURSTS, waitingTime);

    // same scenario but timed out. 100'000 + timeout = 600'000 => timed out since 100'000
    // continue immidiately
    when(stamper.timeStamp()).thenReturn(700000L);
    when(outBuffer.getOldestUnackedTimestamp()).thenReturn(100000L);
    waitingTime = algorithm.getWaitingTimeMicroSeconds();
    assertEquals(0L, waitingTime);
  }

  private UtpTimestampedPacketDTO createPacket(int sequenceNumber, int packetLength) {
    UtpPacket pkt = new UtpPacket();
    pkt.setSequenceNumber(longToUshort(sequenceNumber));
    pkt.setPayload(new byte[packetLength]);
    return new UtpTimestampedPacketDTO(pkt, 1L, 0);
  }

  private UtpTimestampedPacketDTO createPacket(int sequenceNumber) throws SocketException {
    return createPacket(sequenceNumber, 1);
  }
}
