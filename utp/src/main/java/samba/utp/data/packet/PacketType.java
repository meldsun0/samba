package samba.utp.data.packet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 0 4 8 16 24 32 +-------+-------+---------------+---------------+---------------+ | type | ver |
 * extension | connection_id |
 */
public enum PacketType {
  ST_DATA(0x0),
  ST_FIN(0x1),
  ST_STATE(0x2),
  ST_RESET(0x3),
  ST_SYN(0x4);

  private final byte value;
  private static final int MAX_VALUE = 0x4;
  private static final int BYTE_MASK = 0xFF;

  PacketType(int value) {
    checkArgument(value <= MAX_VALUE, "Packet type ID must be in range [0x0, 0x4])");
    this.value = (byte) (value & BYTE_MASK);
  }

  public byte getByteValue() {
    return value;
  }

  public static PacketType fromInt(int value) {
    value = value & BYTE_MASK;
    for (PacketType messageType : PacketType.values()) {
      if (messageType.value == value) {
        return messageType;
      }
    }
    return null;
  }
}
