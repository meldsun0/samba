package samba.domain.messages.extensions.standard;

import samba.domain.types.unsigned.UInt16;

public enum ErrorType {
  EXTENSION_NOT_SUPPORTED(0),
  DATA_NOT_FOUND(1),
  FAILED_TO_DECODE(2),
  SYSTEM_ERROR(3);

  private final int code;

  ErrorType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public UInt16 getErrorCode() {
    return UInt16.valueOf(code);
  }

  public static ErrorType fromCode(int code) {
    for (ErrorType errorType : ErrorType.values()) {
      if (errorType.code == code) {
        return errorType;
      }
    }
    throw new IllegalArgumentException("Unknown error code: " + code);
  }
}
