package samba.network;

import org.apache.tuweni.bytes.Bytes;

public enum NetworkType {
  EXECUTION_STATE_NETWORK(0x500A, "state-network", 2),
  EXECUTION_HISTORY_NETWORK(0x500B, "history-network", 2),
  BEACON_CHAIN_NETWORK(0x500C, "beacon-chain-network", 2),

  ANGELFOOD_STATE_NETWORK(0x504A, "angelfood-state-network", 2),
  ANGELFOOD_HISTORY_NETWORK(0x504B, "angelfood-history-network", 2),
  ANGELFOOOD_BECACON_CHAIN_NETWORK(0x504C, "angelfood-state-network", 2),

  UTP(0x757470, "utp", 3);

  private final int value;
  private final String name;
  private final int byteLength;

  // Constructor with byteLength for each enum value
  NetworkType(int value, String name, int byteLength) {
    this.value = value;
    this.name = name;
    this.byteLength = byteLength;
  }

  // Generalized getValue() method using byteLength
  public Bytes getValue() {
    byte[] byteArray = new byte[byteLength];

    // Fill the byte array based on the byte length
    for (int i = 0; i < byteLength; i++) {
      byteArray[byteLength - 1 - i] = (byte) (value >>> (8 * i));
    }

    return Bytes.wrap(byteArray);
  }

  public String getName() {
    return this.name;
  }

  public boolean isEquals(Bytes bytes) {
    return getValue().equals(bytes);
  }

  public static NetworkType fromString(String networkName) {
    for (NetworkType type : NetworkType.values()) {
      if (type.getName().equalsIgnoreCase(networkName)) {
        return type;
      }
    }
    throw new IllegalArgumentException("No enum constant with name " + networkName);
  }

  public static NetworkType fromBytes(Bytes bytes) {
    for (NetworkType type : NetworkType.values()) {
      if (type.isEquals(bytes)) {
        return type;
      }
    }
    return null;
  }
}
