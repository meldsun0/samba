package samba.network;

import org.apache.tuweni.bytes.Bytes;

public enum NetworkType {

    EXECUTION_STATE_NETWORK(0x500A, "State-Network"),
    EXECUTION_HISTORY_NETWORK(0x500B, "History-Network"),
    BEACON_CHAIN_NETWORK(0x500C, "Beacon-chain-Network"),

    ANGELFOOD_STATE_NETWORK(0x504A, "Angelfood-State-Network"),
    ANGELFOOD_HISTORY_NETWORK(0x504B, "Angelfood-History-Network"),
    ANGELFOOOD_BECACON_CHAIN_NETWORK(0x504C, "Angelfood-State-Network");

    private final byte value;
    private String name;

    NetworkType(int value, String name) {
        this.value = (byte) (value);
        this.name = name;
    }

    public Bytes getValue() {
        return Bytes.wrap(new byte[]{value});
    }
}