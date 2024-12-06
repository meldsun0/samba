package samba.network;

import org.apache.tuweni.bytes.Bytes;

public enum NetworkType {
  EXECUTION_STATE_NETWORK(0x500A, "state-network"),
  EXECUTION_HISTORY_NETWORK(0x500B, "history-network"),
  BEACON_CHAIN_NETWORK(0x500C, "beacon-chain-network"),

  ANGELFOOD_STATE_NETWORK(0x504A, "angelfood-state-network"),
  ANGELFOOD_HISTORY_NETWORK(0x504B, "angelfood-history-network"),
  ANGELFOOOD_BECACON_CHAIN_NETWORK(0x504C, "angelfood-state-network");

  private final int value;
  private String name;

  NetworkType(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public Bytes getValue() {
    return Bytes.wrap(new byte[] {(byte) (value >>> 8), (byte) value});
  }

  public String getName() {
    return this.name;
  }

  public boolean isEquals(Bytes bytes) {
    return getValue().equals(bytes);
  }
}
