package samba.schema.content.ssz.blockheader;

import samba.network.history.HistoryConstants;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderFunctions;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import tech.pegasys.teku.infrastructure.ssz.SszUnion;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class BlockHeaderWithProofContainer
    extends Container2<BlockHeaderWithProofContainer, SszByteList, SszUnion> {

  public BlockHeaderWithProofContainer(
      final BlockHeader blockHeader, final BlockHeaderProofUnion blockHeaderProof) {
    super(
        BlockHeaderWithProofContainerSchema.INSTANCE,
        SszByteListSchema.create(HistoryConstants.MAX_HEADER_LENGTH)
            .fromBytes(rlpEncodeBlockHeader(blockHeader)),
        blockHeaderProof.getUnion());
  }

  public BlockHeaderWithProofContainer(TreeNode backingNode) {
    super(BlockHeaderWithProofContainerSchema.INSTANCE, backingNode);
  }

  private static Bytes rlpEncodeBlockHeader(BlockHeader blockHeader) {
    BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
    blockHeader.writeTo(rlpOutput);
    return rlpOutput.encoded();
  }

  public BlockHeader getBlockHeader() {
    RLPInput input = RLP.input(getField0().getBytes());
    return BlockHeader.readFrom(input, new MainnetBlockHeaderFunctions());
  }

  public BlockHeaderProofUnion getBlockHeaderProof() {
    return new BlockHeaderProofUnion(getField1());
  }

  public static BlockHeaderWithProofContainer decodeBytes(Bytes bytes) {
    BlockHeaderWithProofContainerSchema schema = BlockHeaderWithProofContainerSchema.INSTANCE;
    BlockHeaderWithProofContainer decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public static class BlockHeaderWithProofContainerSchema
      extends ContainerSchema2<BlockHeaderWithProofContainer, SszByteList, SszUnion> {

    public static final BlockHeaderWithProofContainerSchema INSTANCE =
        new BlockHeaderWithProofContainerSchema();

    private BlockHeaderWithProofContainerSchema() {
      super(
          SszByteListSchema.create(HistoryConstants.MAX_HEADER_LENGTH),
          BlockHeaderProofUnion.getSchema());
    }

    @Override
    public BlockHeaderWithProofContainer createFromBackingNode(TreeNode node) {
      return new BlockHeaderWithProofContainer(node);
    }
  }
}
