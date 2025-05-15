package samba.domain.messages.response;

import java.util.HashMap;
import java.util.Map;

public enum AcceptCodes {
  ACCEPT(0x00),
  GENERIC_DECLINE(0x01),
  CONTENT_ALREADY_STORED(0x02),
  CONTENT_NOT_IN_RADIUS(0x03),
  RATE_LIMIT_REACHED(0x04),
  RATE_LIMIT_REACHED_FOR_CONTENT(0x05),
  CONTENT_KEY_NOT_VERIFIABLE(0x06),
  UNSPECIFIED_ERROR(0x07);

  private final byte value;

  private static final Map<Byte, AcceptCodes> BYTE_TO_CODE = new HashMap<>();

  static {
    for (AcceptCodes code : values()) {
      BYTE_TO_CODE.put(code.value, code);
    }
  }

  AcceptCodes(int value) {
    this.value = (byte) value;
  }

  public byte getValue() {
    return value;
  }

  public static AcceptCodes fromByte(byte b) {
    return BYTE_TO_CODE.getOrDefault(b, UNSPECIFIED_ERROR);
  }
}
