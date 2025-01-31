package samba.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetworkTypeTest {

  @Test
  void testEnumNames() {
    assertEquals("state-network", NetworkType.EXECUTION_STATE_NETWORK.getName());
    assertEquals("history-network", NetworkType.EXECUTION_HISTORY_NETWORK.getName());
    assertEquals("beacon-chain-network", NetworkType.BEACON_CHAIN_NETWORK.getName());
    assertEquals("angelfood-state-network", NetworkType.ANGELFOOD_STATE_NETWORK.getName());
    assertEquals("angelfood-history-network", NetworkType.ANGELFOOD_HISTORY_NETWORK.getName());
    assertEquals("angelfood-state-network", NetworkType.ANGELFOOOD_BECACON_CHAIN_NETWORK.getName());
    assertEquals("utp", NetworkType.UTP.getName());
  }

  @Test
  void testGetValue() {
    assertArrayEquals(
        new byte[] {(byte) (0x500A >>> 8), (byte) 0x500A},
        NetworkType.EXECUTION_STATE_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x500B >>> 8), (byte) 0x500B},
        NetworkType.EXECUTION_HISTORY_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x500C >>> 8), (byte) 0x500C},
        NetworkType.BEACON_CHAIN_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x504A >>> 8), (byte) 0x504A},
        NetworkType.ANGELFOOD_STATE_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x504B >>> 8), (byte) 0x504B},
        NetworkType.ANGELFOOD_HISTORY_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x504C >>> 8), (byte) 0x504C},
        NetworkType.ANGELFOOOD_BECACON_CHAIN_NETWORK.getValue().toArray());
    assertArrayEquals(
        new byte[] {(byte) (0x757470 >>> 16), (byte) (0x757470 >>> 8), (byte) 0x757470},
        NetworkType.UTP.getValue().toArray());
  }

  @Test
  void testBinaryRepresentation() {
    assertEquals(
        "0101000000001010",
        String.format("%16s", Integer.toBinaryString(0x500A)).replace(' ', '0'));
    assertEquals(
        "0101000000001011",
        String.format("%16s", Integer.toBinaryString(0x500B)).replace(' ', '0'));
    assertEquals(
        "0101000000001100",
        String.format("%16s", Integer.toBinaryString(0x500C)).replace(' ', '0'));
    assertEquals(
        "0101000001001010",
        String.format("%16s", Integer.toBinaryString(0x504A)).replace(' ', '0'));
    assertEquals(
        "0101000001001011",
        String.format("%16s", Integer.toBinaryString(0x504B)).replace(' ', '0'));
    assertEquals(
        "0101000001001100",
        String.format("%16s", Integer.toBinaryString(0x504C)).replace(' ', '0'));
    assertEquals(
        "011101010111010001110000",
        String.format("%24s", Integer.toBinaryString(0x757470)).replace(' ', '0'));
  }
}
