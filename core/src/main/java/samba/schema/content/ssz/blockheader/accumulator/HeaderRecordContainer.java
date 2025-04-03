package samba.schema.content.ssz.blockheader.accumulator;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt256;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class HeaderRecordContainer extends Container2<HeaderRecordContainer, SszBytes32, SszUInt256> {
    
    public HeaderRecordContainer(Bytes32 blockHash, UInt256 totalDifficulty) {
        super(HeaderRecordContainerSchema.INSTANCE, SszBytes32.of(blockHash), SszUInt256.of(totalDifficulty));
    }

    public HeaderRecordContainer(TreeNode backingNode) {
        super(HeaderRecordContainerSchema.INSTANCE, backingNode);
    }

    public Bytes32 getBlockHash() {
        return getField0().get();
    }

    public UInt256 getTotalDifficulty() {
        return getField1().get();
    }

    public static HeaderRecordContainer decodeBytes(Bytes sszBytes) {
        return HeaderRecordContainerSchema.INSTANCE.sszDeserialize(sszBytes);
    }

    public static class HeaderRecordContainerSchema extends ContainerSchema2<HeaderRecordContainer, SszBytes32, SszUInt256> {
        public static final HeaderRecordContainerSchema INSTANCE = new HeaderRecordContainerSchema();

        private HeaderRecordContainerSchema() {
            super(SszPrimitiveSchemas.BYTES32_SCHEMA, SszPrimitiveSchemas.UINT256_SCHEMA);
        }

        @Override
        public HeaderRecordContainer createFromBackingNode(TreeNode backingNode) {
            return new HeaderRecordContainer(backingNode);
        }
    }
}
