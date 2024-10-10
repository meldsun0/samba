package samba.schema.ssz.containers;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt256;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;


public class HistoryCustomPayloadContainer extends Container1<HistoryCustomPayloadContainer, SszUInt256> {

    public HistoryCustomPayloadContainer(Bytes data_radius) {
        super(HistoryCustomPayloadContainerSchema.INSTANCE, SszPrimitiveSchemas.UINT256_SCHEMA.sszDeserialize(data_radius));
    }

    public HistoryCustomPayloadContainer(TreeNode backingNode) {
        super(HistoryCustomPayloadContainerSchema.INSTANCE, backingNode);
    }

    public UInt256 getDataRadius() {
        return  SszPrimitiveSchemas.UINT256_SCHEMA.sszDeserialize(getField0().get()).get();
    }


    public static HistoryCustomPayloadContainer decodePacket(Bytes packet) {
        HistoryCustomPayloadContainerSchema schema = HistoryCustomPayloadContainerSchema.INSTANCE;
        HistoryCustomPayloadContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }

    public static class HistoryCustomPayloadContainerSchema extends ContainerSchema1<HistoryCustomPayloadContainer, SszUInt256> {

        public static final HistoryCustomPayloadContainerSchema INSTANCE = new HistoryCustomPayloadContainerSchema();

        private HistoryCustomPayloadContainerSchema() {
            super(SszPrimitiveSchemas.UINT256_SCHEMA);
        }

        @Override
        public HistoryCustomPayloadContainer createFromBackingNode(TreeNode node) {
            return new HistoryCustomPayloadContainer(node);
        }
    }
}
