package samba.domain.messages.extensions;

import samba.domain.types.unsigned.UInt16;

public enum ExtensionType {
  CLIENT_INFO_AND_CAPABILITIES(0),
  BASIC_RADIUS(1),
  HISTORY_RADIUS(2),
  ERROR(65535);

  private final int value;

  ExtensionType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public UInt16 getExtensionCode() {
    return UInt16.valueOf(value);
  }

  public static ExtensionType fromValue(int value) {
    for (ExtensionType extensionType : ExtensionType.values()) {
      if (extensionType.value == value) {
        return extensionType;
      }
    }
    throw new IllegalArgumentException("Unknown extension type: " + value);
  }

  public static ExtensionType fromValue(UInt16 value) {
    return fromValue(value.getValue());
  }
}
