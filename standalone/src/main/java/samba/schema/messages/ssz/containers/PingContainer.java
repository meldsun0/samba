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

public class PingContainer extends Container3<PingContainer, SszUInt64, SszUInt16, SszByteList> {

  public PingContainer(UInt64 enrSeq, UInt16 payloadType, Bytes payload) {
    super(
        PingSchema.INSTANCE,
        SszUInt64.of(enrSeq),
        SszUInt16.of(payloadType),
        SszByteListSchema.create(PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES).fromBytes(payload));
  }

  public PingContainer(TreeNode backingNode) {
    super(PingSchema.INSTANCE, backingNode);
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

  public static PingContainer decodePacket(Bytes packet) {
    PingSchema schema = PingSchema.INSTANCE;
    PingContainer decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class PingSchema
      extends ContainerSchema3<PingContainer, SszUInt64, SszUInt16, SszByteList> {

    public static final PingSchema INSTANCE = new PingSchema();

    private PingSchema() {
      super(
          SszPrimitiveSchemas.UINT64_SCHEMA,
          SszUInt16.UINT16_SCHEMA,
          SszByteListSchema.create(PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES));
    }

    @Override
    public PingContainer createFromBackingNode(TreeNode node) {
      return new PingContainer(node);
    }
  }
}
