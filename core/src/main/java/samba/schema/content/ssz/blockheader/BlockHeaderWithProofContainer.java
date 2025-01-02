package samba.schema.content.ssz.blockheader;

import org.apache.tuweni.bytes.Bytes;

import samba.network.history.HistoryConstants;
import tech.pegasys.teku.infrastructure.ssz.SszUnion;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class BlockHeaderWithProofContainer extends 
    Container2<BlockHeaderWithProofContainer,
    SszByteList,
    SszUnion> {

    public BlockHeaderWithProofContainer(
        final Bytes blockHeader,
        final BlockHeaderProofUnion blockHeaderProof) {
            super(
                BlockHeaderWithProofContainerSchema.INSTANCE,
                SszByteListSchema.create(HistoryConstants.MAX_HEADER_LENGTH).fromBytes(blockHeader),
                blockHeaderProof.getUnion());
    }

    public BlockHeaderWithProofContainer(TreeNode backingNode) {
        super(BlockHeaderWithProofContainerSchema.INSTANCE, backingNode);
    }

    public Bytes getBlockHeader() {
        return getField0().getBytes();
    }

    public BlockHeaderProofUnion getBlockHeaderProof() {
        return new BlockHeaderProofUnion(getField1());
    }

    public static BlockHeaderWithProofContainer decodeBytes(Bytes bytes) {
        BlockHeaderWithProofContainerSchema schema = BlockHeaderWithProofContainerSchema.INSTANCE;
        BlockHeaderWithProofContainer decodedBytes = schema.sszDeserialize(bytes);
        return decodedBytes;
    }

    public static class BlockHeaderWithProofContainerSchema extends ContainerSchema2<BlockHeaderWithProofContainer, SszByteList, SszUnion> {
        public static final BlockHeaderWithProofContainerSchema INSTANCE = new BlockHeaderWithProofContainerSchema();

        private BlockHeaderWithProofContainerSchema() {
            super(SszByteListSchema.create(HistoryConstants.MAX_HEADER_LENGTH), BlockHeaderProofUnion.getSchema());
        }

        @Override
        public BlockHeaderWithProofContainer createFromBackingNode(TreeNode node) {
            return new BlockHeaderWithProofContainer(node);
        }
    }
}