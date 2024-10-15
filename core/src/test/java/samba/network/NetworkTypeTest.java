package samba.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NetworkTypeTest {

    @Test
    void testEnumValues() {
        assertEquals(0x500A, NetworkType.EXECUTION_STATE_NETWORK.getIntValue());
        assertEquals(0x500B, NetworkType.EXECUTION_HISTORY_NETWORK.getIntValue());
        assertEquals(0x500C, NetworkType.BEACON_CHAIN_NETWORK.getIntValue());
        assertEquals(0x504A, NetworkType.ANGELFOOD_STATE_NETWORK.getIntValue());
        assertEquals(0x504B, NetworkType.ANGELFOOD_HISTORY_NETWORK.getIntValue());
        assertEquals(0x504C, NetworkType.ANGELFOOOD_BECACON_CHAIN_NETWORK.getIntValue());
    }

    @Test
    void testEnumNames() {
        assertEquals("State-Network", NetworkType.EXECUTION_STATE_NETWORK.getName());
        assertEquals("History-Network", NetworkType.EXECUTION_HISTORY_NETWORK.getName());
        assertEquals("Beacon-chain-Network", NetworkType.BEACON_CHAIN_NETWORK.getName());
        assertEquals("Angelfood-State-Network", NetworkType.ANGELFOOD_STATE_NETWORK.getName());
        assertEquals("Angelfood-History-Network", NetworkType.ANGELFOOD_HISTORY_NETWORK.getName());
        assertEquals("Angelfood-State-Network", NetworkType.ANGELFOOOD_BECACON_CHAIN_NETWORK.getName());
    }

    @Test
    void testGetValue() {
        assertArrayEquals(new byte[]{(byte) (0x500A >>> 8), (byte) 0x500A}, NetworkType.EXECUTION_STATE_NETWORK.getValue().toArray());
        assertArrayEquals(new byte[]{(byte) (0x500B >>> 8), (byte) 0x500B}, NetworkType.EXECUTION_HISTORY_NETWORK.getValue().toArray());
        assertArrayEquals(new byte[]{(byte) (0x500C >>> 8), (byte) 0x500C}, NetworkType.BEACON_CHAIN_NETWORK.getValue().toArray());
        assertArrayEquals(new byte[]{(byte) (0x504A >>> 8), (byte) 0x504A}, NetworkType.ANGELFOOD_STATE_NETWORK.getValue().toArray());
        assertArrayEquals(new byte[]{(byte) (0x504B >>> 8), (byte) 0x504B}, NetworkType.ANGELFOOD_HISTORY_NETWORK.getValue().toArray());
        assertArrayEquals(new byte[]{(byte) (0x504C >>> 8), (byte) 0x504C}, NetworkType.ANGELFOOOD_BECACON_CHAIN_NETWORK.getValue().toArray());
    }

    @Test
    void testBinaryRepresentation() {
        assertEquals("0101000000001010", String.format("%16s", Integer.toBinaryString(0x500A)).replace(' ', '0'));
        assertEquals("0101000000001011", String.format("%16s", Integer.toBinaryString(0x500B)).replace(' ', '0'));
        assertEquals("0101000000001100", String.format("%16s", Integer.toBinaryString(0x500C)).replace(' ', '0'));
        assertEquals("0101000001001010", String.format("%16s", Integer.toBinaryString(0x504A)).replace(' ', '0'));
        assertEquals("0101000001001011", String.format("%16s", Integer.toBinaryString(0x504B)).replace(' ', '0'));
        assertEquals("0101000001001100", String.format("%16s", Integer.toBinaryString(0x504C)).replace(' ', '0'));
    }
}
