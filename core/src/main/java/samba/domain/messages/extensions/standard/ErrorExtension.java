package samba.domain.messages.extensions.standard;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.extensions.PortalExtension;
import samba.domain.types.unsigned.UInt16;
import samba.schema.messages.ssz.containers.extensions.standard.ErrorContainer;

import org.apache.tuweni.bytes.Bytes;

public class ErrorExtension implements PortalExtension {

  private final UInt16 errorCode;
  private final String message;

  public ErrorExtension(UInt16 errorCode, String message) {
    checkArgument(errorCode != null, "Error code cannot be null");
    checkArgument(
        message.length() <= MAX_ERROR_BYTE_LENGTH, "Error message length exceeds maximum length");
    this.errorCode = errorCode;
    this.message = message;
  }

  public ErrorExtension(UInt16 errorCode) {
    this(errorCode, "");
  }

  public static ErrorExtension fromSszBytes(Bytes sszBytes) {
    ErrorContainer container = ErrorContainer.decode(sszBytes);
    return new ErrorExtension(container.getErrorCode(), container.getMessage());
  }

  @Override
  public ExtensionType ExtensionType() {
    return ExtensionType.ERROR;
  }

  @Override
  public ErrorExtension getExtension() {
    return this;
  }

  public UInt16 getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public Bytes getSszBytes() {
    return new ErrorContainer(errorCode, message).sszSerialize();
  }
}
