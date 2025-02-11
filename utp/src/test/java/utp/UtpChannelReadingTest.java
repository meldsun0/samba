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
package utp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUbyte;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;
import static utp.data.bytes.BinaryToStringTestHelper.toBinaryString;

import samba.utp.SessionState;
import samba.utp.UTPClient;
import samba.utp.algo.UtpAlgConfiguration;
import samba.utp.data.UtpHeaderExtension;
import samba.utp.data.UtpPacket;
import samba.utp.data.UtpPacketUtils;
import samba.utp.network.udp.UDPAddress;
import samba.utp.network.udp.UDPTransportLayer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.ArgumentCaptor;

public class UtpChannelReadingTest {

  @Test
  public void test() throws InterruptedException, IllegalAccessException, ExecutionException {
    UtpAlgConfiguration.AUTO_ACK_SMALLER_THAN_ACK_NUMBER = false;
    UtpAlgConfiguration.SKIP_PACKETS_UNTIL_ACK = 1;

    UDPTransportLayer udpTransportLayer = mock(UDPTransportLayer.class);
    UDPAddress remoteAddress = mock(UDPAddress.class);

    UTPClient channel = new UTPClient(udpTransportLayer);
    channel.setState(SessionState.CONNECTED);

    injectPriveteField(channel, "connection", CompletableFuture.completedFuture(null));

    channel.setAckNumber(
        2); // last recieved packet has seqNr. 2, next one will be packet with seqNr. 3

    /*
     * argument captor on socket, which will record all invocations of socket.send(packet)
     * and capture the arguments, in this case the arguments are ack packets.
     */
    ArgumentCaptor<UtpPacket> ackOne = ArgumentCaptor.forClass(UtpPacket.class);

    try {
      channel.receivePacket(createPacket(3), remoteAddress); // ack 3
      channel.receivePacket(createPacket(4), remoteAddress); // ack 4
      channel.receivePacket(createPacket(6), remoteAddress); // ack 4, Sack 6 -> 00000001
      channel.receivePacket(createPacket(8), remoteAddress); // ack 4, Sack 6, 8 -> 00000101
      channel.receivePacket(createPacket(5), remoteAddress); // ack 6, sack 8 -> 00000001
      channel.receivePacket(createPacket(7), remoteAddress); // ack 8

      Bytes bytesRead = channel.read().get();

      // verify 6 ack packets where send and capture them
      verify(udpTransportLayer, times(6))
          .sendPacket(ackOne.capture(), ArgumentCaptor.forClass(UDPAddress.class).capture());
      List<UtpPacket> allValues = ackOne.getAllValues();
      Iterator<UtpPacket> iterator = allValues.iterator();

      // extract utp packets from the udp packets.
      UtpPacket three = iterator.next();
      UtpPacket four = iterator.next();
      UtpPacket six = iterator.next();
      UtpPacket eight = iterator.next();
      UtpPacket five = iterator.next();
      UtpPacket seven = iterator.next();

      // first two packets were acked normally
      testPacket(three, 3, null, UtpPacketUtils.STATE);
      testPacket(four, 4, null, UtpPacketUtils.STATE);

      // packet six recieved before packet 5: [Ack:4; SACK:6]
      String selAckSix = "00000001" + "00000000" + "00000000" + "00000000";
      testPacket(six, 4, selAckSix, UtpPacketUtils.STATE);

      // packet 8 recieved before packet 5: [Ack: 4; SACK:6,8]
      String selAckeight = "00000101" + "00000000" + "00000000" + "00000000";
      testPacket(eight, 4, selAckeight, UtpPacketUtils.STATE);

      // packet 5 recieved, so everything recieved up to packet 6: [Ack:6, SACK:8]
      String selAckfive = "00000001" + "00000000" + "00000000" + "00000000";
      testPacket(five, 6, selAckfive, UtpPacketUtils.STATE);

      // everything recieved up till packet 8, means job done: [Ack:8]
      testPacket(seven, 8, null, UtpPacketUtils.STATE);

      ByteBuffer buffer = ByteBuffer.wrap(bytesRead.toArray());

      // buffer should have 6'000 bytes in it
      assertEquals(6000, buffer.limit());

      // read from buffer
      byte[] third = new byte[1000];
      buffer.get(third);
      byte[] fourth = new byte[1000];
      buffer.get(fourth);
      byte[] fifth = new byte[1000];
      buffer.get(fifth);
      byte[] sixt = new byte[1000];
      buffer.get(sixt);
      byte[] seventh = new byte[1000];
      buffer.get(seventh);
      byte[] eighth = new byte[1000];
      buffer.get(eighth);

      /*
       *  test recieved data. first 1000 bytes each should == 3,
       *  second 1000 bytes each should == 4
       *  etc.
       */
      assertArrayEquals(getPayload(3), third);
      assertArrayEquals(getPayload(4), fourth);
      assertArrayEquals(getPayload(5), fifth);
      assertArrayEquals(getPayload(6), sixt);
      assertArrayEquals(getPayload(7), seventh);
      assertArrayEquals(getPayload(8), eighth);

      /*
       * check buffer is now empty
       */
      assertFalse(buffer.hasRemaining());

    } catch (IOException e) {
      fail("Exception occured but was not expected");
      e.printStackTrace();
    }
  }

  private static void injectPriveteField(Object object, String attributeName, Object attributeValue)
      throws IllegalAccessException {
    Field field =
        ReflectionUtils.findFields(
                object.getClass(),
                f -> f.getName().equals(attributeName),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);
    field.setAccessible(true);
    field.set(object, attributeValue);
  }

  private void testPacket(UtpPacket pkt, int seq, String selAck, byte type) {

    assertEquals(longToUshort(seq), pkt.getAckNumber());
    assertEquals(type, pkt.getTypeVersion());

    if (selAck != null) {
      UtpHeaderExtension[] ext = pkt.getExtensions();
      if (ext == null || ext.length != 1) {
        fail("expecting selective ack extension, but got none here or more than 1");
      }
      UtpHeaderExtension selectiveAck = ext[0];
      assertEquals(longToUbyte(0), selectiveAck.getNextExtension());
      assertEquals(longToUbyte(4), selectiveAck.getLength());
      String selAckString =
          toBinaryString(selectiveAck.getBitMask()[0])
              + toBinaryString(selectiveAck.getBitMask()[1])
              + toBinaryString(selectiveAck.getBitMask()[2])
              + toBinaryString(selectiveAck.getBitMask()[3]);
      assertEquals(selAck, selAckString);
    }
  }

  public UtpPacket createPacket(int seqNumber) {
    UtpPacket utpPacket = new UtpPacket();
    utpPacket.setSequenceNumber(longToUshort(seqNumber));
    utpPacket.setTypeVersion(UtpPacketUtils.DATA);
    utpPacket.setPayload(getPayload(seqNumber));
    utpPacket.setWindowSize(1);
    return utpPacket;
  }

  private byte[] getPayload(int seqNumber) {
    byte[] array = new byte[1000];
    for (int i = 0; i < 1000; i++) {
      array[i] = (byte) seqNumber;
    }
    return array;
  }
}
