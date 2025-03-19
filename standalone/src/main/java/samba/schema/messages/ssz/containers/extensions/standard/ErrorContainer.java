package samba.schema.messages.ssz.containers.extensions.standard;

import samba.domain.messages.extensions.PortalExtension;
import samba.domain.types.unsigned.UInt16;
import samba.schema.primitives.SszUInt16;

import java.nio.charset.StandardCharsets;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class ErrorContainer extends Container2<ErrorContainer, SszUInt16, SszByteList> {

  public ErrorContainer(UInt16 errorCode, String message) {
    super(
        ErrorSchema.INSTANCE,
        SszUInt16.of(errorCode),
        SszByteListSchema.create(PortalExtension.MAX_ERROR_BYTE_LENGTH)
            .fromBytes(Bytes.wrap(message.getBytes(StandardCharsets.UTF_8))));
  }

  public ErrorContainer(TreeNode backingNode) {
    super(ErrorSchema.INSTANCE, backingNode);
  }

  public UInt16 getErrorCode() {
    return getField0().get();
  }

  public String getMessage() {
    return new String(getField1().getBytes().toArray(), StandardCharsets.UTF_8);
  }

  public static ErrorContainer decode(Bytes sszBytes) {
    ErrorSchema schema = ErrorSchema.INSTANCE;
    ErrorContainer decodedBytes = schema.sszDeserialize(sszBytes);
    return decodedBytes;
  }

  public static class ErrorSchema extends ContainerSchema2<ErrorContainer, SszUInt16, SszByteList> {

    public static final ErrorSchema INSTANCE = new ErrorSchema();

    private ErrorSchema() {
      super(
          SszUInt16.UINT16_SCHEMA, SszByteListSchema.create(PortalExtension.MAX_ERROR_BYTE_LENGTH));
    }

    @Override
    public ErrorContainer createFromBackingNode(TreeNode node) {
      return new ErrorContainer(node);
    }
  }
}
