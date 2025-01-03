package samba.schema.content.ssz.blockbody;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class BlockBodyPreShanghaiContainer
    extends Container2<BlockBodyPreShanghaiContainer, SszList<SszByteList>, SszByteList> {

  public BlockBodyPreShanghaiContainer(List<Transaction> transactions, List<BlockHeader> uncles) {
    super(
        BlockBodyPreShanghaiContainerSchema.INSTANCE,
        SszTransactionList.createSszList(transactions),
        SszUnclesByteList.createSszUncles(uncles));
  }

  public BlockBodyPreShanghaiContainer(TreeNode backingNode) {
    super(BlockBodyPreShanghaiContainerSchema.INSTANCE, backingNode);
  }

  public List<Transaction> getTransactions() {
    return SszTransactionList.decodeSszList(getField0());
  }

  public List<BlockHeader> getUncles() {
    return SszUnclesByteList.decodeSszUncles(getField1());
  }

  public static BlockBodyPreShanghaiContainer decodeBytes(Bytes bytes) {
    BlockBodyPreShanghaiContainerSchema schema = BlockBodyPreShanghaiContainerSchema.INSTANCE;
    BlockBodyPreShanghaiContainer decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public static class BlockBodyPreShanghaiContainerSchema
      extends ContainerSchema2<BlockBodyPreShanghaiContainer, SszList<SszByteList>, SszByteList> {

    public static final BlockBodyPreShanghaiContainerSchema INSTANCE =
        new BlockBodyPreShanghaiContainerSchema();

    private BlockBodyPreShanghaiContainerSchema() {
      super(SszTransactionList.getSchema(), SszUnclesByteList.getSchema());
    }

    @Override
    public BlockBodyPreShanghaiContainer createFromBackingNode(TreeNode node) {
      return new BlockBodyPreShanghaiContainer(node);
    }
  }
}
