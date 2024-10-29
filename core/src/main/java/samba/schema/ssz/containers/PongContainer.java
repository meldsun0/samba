package samba.schema.ssz.containers;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.PortalWireMessage;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;

import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PongContainer extends Container2<PongContainer, SszUInt64, SszByteList> {
    
    public PongContainer(UInt64 enrSeq, Bytes customPayload) {
        super(PongSchema.INSTANCE, SszUInt64.of(enrSeq), SszByteListSchema.create(2048).fromBytes(customPayload));
    }

    public PongContainer(TreeNode backingNode) {
        super(PongSchema.INSTANCE, backingNode);
    }

    public UInt64 getEnrSeq() {
        return getField0().get();
    }

    public Bytes getCustomPayload() {
        return getField1().getBytes();
    }

    public static PongContainer decodePacket(Bytes packet) {
        PongSchema schema = PongSchema.INSTANCE;
        PongContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }

    public static class PongSchema extends ContainerSchema2<PongContainer, SszUInt64, SszByteList> {

        public static final PongSchema INSTANCE = new PongSchema();

        private PongSchema() {
            super(SszPrimitiveSchemas.UINT64_SCHEMA, SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES));
        }

        @Override
        public PongContainer createFromBackingNode(TreeNode node) {
            return new PongContainer(node);
        }
    }
}