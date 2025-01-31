package utp.data.bytes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static samba.utp.data.UtpPacketUtils.*;
import static samba.utp.data.bytes.UnsignedTypesUtil.*;

import samba.utp.data.SelectiveAckHeaderExtension;
import samba.utp.data.UtpHeaderExtension;
import samba.utp.data.UtpPacket;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Following
 * https://github.com/ethereum/portal-network-specs/blob/master/utp/utp-wire-test-vectors.md
 */
public class UtpPacketTests {

  @Test
  public void testSYNPacketHeader() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(SYN)
            .connectionId(longToUshort(10049))
            .timestamp(longToUint(3384187322L))
            .timestampDifference(0)
            .windowSize(1048576)
            .ackNumber(longToUshort(0))
            .sequenceNumber(longToUshort(11884))
            .build();
    assertEquals(
        "0x41002741c9b699ba00000000001000002e6c0000".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }

  @Test
  public void testACKPacketHeader() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(STATE)
            .connectionId(longToUshort(10049))
            .timestamp(longToUint(6195294))
            .timestampDifference(916973699)
            .windowSize(1048576)
            .ackNumber(longToUshort(11885))
            .sequenceNumber(longToUshort(16807))
            .build();
    assertEquals(
        "0x21002741005e885e36a7e8830010000041a72e6d".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }

  @Test
  public void testACKPacketHeaderWithSelectiveACKExtension() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(STATE)
            .connectionId(longToUshort(10049))
            .timestamp(longToUint(6195294))
            .timestampDifference(916973699)
            .windowSize(1048576)
            .ackNumber(longToUshort(11885))
            .sequenceNumber(longToUshort(16807))
            .build();
    utpPacket.setFirstExtension(SELECTIVE_ACK);

    SelectiveAckHeaderExtension selectiveAck = new SelectiveAckHeaderExtension();
    byte[] bitMask = {longToUbyte(1), longToUbyte(0), longToUbyte(0), longToUbyte(128)};
    selectiveAck.setBitMask(bitMask);
    UtpHeaderExtension[] extensions = {selectiveAck};
    utpPacket.setExtensions(extensions);
    assertEquals(
        "0x21012741005e885e36a7e8830010000041a72e6d000401000080".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }

  @Test
  public void testDATAPacketHeader() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(DATA)
            .connectionId(longToUshort(26237))
            .timestamp(longToUint(252492495))
            .timestampDifference(242289855)
            .windowSize(1048576)
            .ackNumber(longToUshort(16806))
            .sequenceNumber(longToUshort(8334))
            .build();
    utpPacket.setPayload(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    assertEquals(
        "0x0100667d0f0cbacf0e710cbf00100000208e41a600010203040506070809".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }

  @Test
  public void testFINPacketHeader() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(FIN)
            .connectionId(longToUshort(19003))
            .timestamp(longToUint(515227279))
            .timestampDifference(511481041)
            .windowSize(1048576)
            .ackNumber(longToUshort(16806))
            .sequenceNumber(longToUshort(41050))
            .build();
    assertEquals(
        "0x11004a3b1eb5be8f1e7c94d100100000a05a41a6".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }

  @Test
  public void testRESETPacketHeader() {
    UtpPacket utpPacket =
        UtpPacket.builder()
            .typeVersion(RESET)
            .connectionId(longToUshort(62285))
            .timestamp(longToUint(751226811))
            .timestampDifference(0)
            .windowSize(0)
            .ackNumber(longToUshort(16807))
            .sequenceNumber(longToUshort(55413))
            .build();
    assertEquals(
        "0x3100f34d2cc6cfbb0000000000000000d87541a7".toUpperCase(),
        Bytes.of(utpPacket.toByteArray()).toHexString().toUpperCase());
  }
}
