package samba.schema.messages.ssz.containers;

import samba.domain.messages.PortalWireMessage;
import samba.domain.types.unsigned.UInt16;
import samba.schema.primitives.SszUInt16;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container3;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema3;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PongContainer extends Container3<PongContainer, SszUInt64, SszUInt16, SszByteList> {

  public PongContainer(UInt64 enrSeq, UInt16 payloadType, Bytes customPayload) {
    super(
        PongSchema.INSTANCE,
        SszUInt64.of(enrSeq),
        SszUInt16.of(payloadType),
        SszByteListSchema.create(PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES)
            .fromBytes(customPayload));
  }

  public PongContainer(TreeNode backingNode) {
    super(PongSchema.INSTANCE, backingNode);
  }

  public UInt64 getEnrSeq() {
    return getField0().get();
  }

  public UInt16 getPayloadType() {
    return getField1().get();
  }

  public Bytes getPayload() {
    return getField2().getBytes();
  }

  public static PongContainer decodePacket(Bytes packet) {
    PongSchema schema = PongSchema.INSTANCE;
    PongContainer decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class PongSchema
      extends ContainerSchema3<PongContainer, SszUInt64, SszUInt16, SszByteList> {

    public static final PongSchema INSTANCE = new PongSchema();

    private PongSchema() {
      super(
          SszPrimitiveSchemas.UINT64_SCHEMA,
          SszUInt16.UINT16_SCHEMA,
          SszByteListSchema.create(PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES));
    }

    @Override
    public PongContainer createFromBackingNode(TreeNode node) {
      return new PongContainer(node);
    }
  }
}
