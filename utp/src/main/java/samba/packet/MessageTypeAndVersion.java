package samba.packet;

import static com.google.common.base.Preconditions.checkArgument;
/**
 * 0       4       8               16              24              32
 * +-------+-------+---------------+---------------+---------------+
 * | type  | ver   | extension     | connection_id                 |
 */
public enum MessageTypeAndVersion {
  ST_DATA(0x01),//1
  ST_FIN(0x11),//17
  ST_STATE(0x21),//33
  ST_RESET(0x31),//49
  ST_SYN(0x41);//65

  private final byte value;
  private static final int MAX_VALUE = 0x41;
  private static final int BYTE_MASK = 0xFF;

  MessageTypeAndVersion(int value) {
    checkArgument(value <= MAX_VALUE, "Packet type ID must be in range [0x01, 0x41)");
    this.value = (byte) (value & BYTE_MASK);
  }

  public byte getByteValue() {
    return value;
  }

  public static MessageTypeAndVersion fromInt(int value) {
    value = value & BYTE_MASK;
    for (MessageTypeAndVersion messageType : MessageTypeAndVersion.values()) {
      if (messageType.value == value) {
        return messageType;
      }
    }
    return null;
  }
}
