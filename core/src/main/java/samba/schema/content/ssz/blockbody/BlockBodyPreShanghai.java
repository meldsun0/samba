package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class BlockBodyPreShanghai
    extends Container2<BlockBodyPreShanghai, SszList<SszByteList>, SszByteList> {

  public BlockBodyPreShanghai(List<Bytes> transactions, Bytes uncles) {
    super(
        BlockBodyPreShanghaiSchema.INSTANCE,
        createSszBytesList(transactions),
        SszByteListSchema.create(HistoryConstants.MAX_ENCODED_UNCLES_LENGTH).fromBytes(uncles));
  }

  public BlockBodyPreShanghai(TreeNode backingNode) {
    super(BlockBodyPreShanghaiSchema.INSTANCE, backingNode);
  }

  private static SszList<SszByteList> createSszBytesList(List<Bytes> transactions) {
    SszByteListSchema byteListSchema =
        SszByteListSchema.create(HistoryConstants.MAX_TRANSACTION_LENGTH);
    List<SszByteList> sszByteLists =
        transactions.stream().map(byteListSchema::fromBytes).collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT)
            .createFromElements(sszByteLists);
  }

  public static class BlockBodyPreShanghaiSchema
      extends ContainerSchema2<BlockBodyPreShanghai, SszList<SszByteList>, SszByteList> {

    public static final BlockBodyPreShanghaiSchema INSTANCE = new BlockBodyPreShanghaiSchema();

    private BlockBodyPreShanghaiSchema() {
      super(
          "BlockBodyPreShanghai",
          namedSchema("transactions", createByteListListSchema()),
          namedSchema(
              "uncles", SszByteListSchema.create(HistoryConstants.MAX_ENCODED_UNCLES_LENGTH)));
    }

    private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
      SszByteListSchema byteListSchema =
          SszByteListSchema.create(HistoryConstants.MAX_TRANSACTION_LENGTH);
      return SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT);
    }

    @Override
    public BlockBodyPreShanghai createFromBackingNode(TreeNode node) {
      return new BlockBodyPreShanghai(node);
    }
  }
}
