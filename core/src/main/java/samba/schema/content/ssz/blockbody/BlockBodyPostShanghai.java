package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container3;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema3;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class BlockBodyPostShanghai
    extends Container3<
        BlockBodyPostShanghai, SszList<SszByteList>, SszByteList, SszList<SszByteList>> {
  public BlockBodyPostShanghai(List<Bytes> transactions, Bytes uncles, List<Bytes> withdrawals) {
    super(
        BlockBodyPostShanghaiSchema.INSTANCE,
        createSszBytesList(transactions),
        SszByteListSchema.create(HistoryConstants.MAX_ENCODED_UNCLES_LENGTH).fromBytes(uncles),
        createSszBytesList(withdrawals));
  }

  public BlockBodyPostShanghai(TreeNode backingNode) {
    super(BlockBodyPostShanghaiSchema.INSTANCE, backingNode);
  }

  private static SszList<SszByteList> createSszBytesList(List<Bytes> list) {
    SszByteListSchema byteListSchema =
        SszByteListSchema.create(HistoryConstants.MAX_TRANSACTION_LENGTH);
    List<SszByteList> sszByteLists =
        list.stream().map(byteListSchema::fromBytes).collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT)
            .createFromElements(sszByteLists);
  }

  public static class BlockBodyPostShanghaiSchema
      extends ContainerSchema3<
          BlockBodyPostShanghai, SszList<SszByteList>, SszByteList, SszList<SszByteList>> {

    public static final BlockBodyPostShanghaiSchema INSTANCE = new BlockBodyPostShanghaiSchema();

    private BlockBodyPostShanghaiSchema() {
      super(
          "BlockBodyPostShanghai",
          namedSchema("transactions", createByteListListSchema()),
          namedSchema(
              "uncles", SszByteListSchema.create(HistoryConstants.MAX_ENCODED_UNCLES_LENGTH)),
          namedSchema("withdrawals", createByteListListSchema()));
    }

    private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
      SszByteListSchema byteListSchema =
          SszByteListSchema.create(HistoryConstants.MAX_TRANSACTION_LENGTH);
      return SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT);
    }

    @Override
    public BlockBodyPostShanghai createFromBackingNode(TreeNode node) {
      return new BlockBodyPostShanghai(node);
    }
  }
}
