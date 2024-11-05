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

public class PingContainer extends Container2<PingContainer, SszUInt64, SszByteList> {
    
    public PingContainer(UInt64 enrSeq, Bytes customPayload) {
        super(PingSchema.INSTANCE, SszUInt64.of(enrSeq), SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES).fromBytes(customPayload));
    }

    public PingContainer(TreeNode backingNode) {
        super(PingSchema.INSTANCE, backingNode);
    }

    public UInt64 getEnrSeq() {
        return getField0().get();
    }

    public Bytes getCustomPayload() {
        return getField1().getBytes();
    }

    public static PingContainer decodePacket(Bytes packet) {
        PingSchema schema = PingSchema.INSTANCE;
        PingContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }



    public static class PingSchema extends ContainerSchema2<PingContainer, SszUInt64, SszByteList> {

        public static final PingSchema INSTANCE = new PingSchema();

        private PingSchema() {
            super(SszPrimitiveSchemas.UINT64_SCHEMA, SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES));
        }

        @Override
        public PingContainer createFromBackingNode(TreeNode node) {
            return new PingContainer(node);
        }
    }
}


