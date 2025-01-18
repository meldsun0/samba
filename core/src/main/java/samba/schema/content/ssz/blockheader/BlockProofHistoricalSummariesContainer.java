package samba.schema.content.ssz.blockheader;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.containers.Container4;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema4;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class BlockProofHistoricalSummariesContainer
    extends Container4<
        BlockProofHistoricalSummariesContainer,
        SszBytes32Vector,
        SszBytes32,
        SszList<SszBytes32>,
        SszUInt64> {

  public BlockProofHistoricalSummariesContainer(
      final List<Bytes32> beaconBlockProofHistoricalSummaries,
      final Bytes32 blockRoot,
      final List<Bytes32> executionBlockProof,
      final UInt64 slot) {
    super(
        BlockProofHistoricalSummariesContainerSchema.INSTANCE,
        SszBeaconBlockProofHistoricalSummariesVector.createVector(
            beaconBlockProofHistoricalSummaries),
        SszBytes32.of(blockRoot),
        SszExecutionBlockProofCapellaList.createList(executionBlockProof),
        SszUInt64.of(slot));
  }

  public BlockProofHistoricalSummariesContainer(TreeNode backingNode) {
    super(BlockProofHistoricalSummariesContainerSchema.INSTANCE, backingNode);
  }

  public List<Bytes32> getBeaconBlockProofHistoricalSummaries() {
    return getField0().asListUnboxed();
  }

  public Bytes32 getBlockRoot() {
    return getField1().get();
  }

  public List<Bytes32> getExecutionBlockProof() {
    return getField2().asList().stream().map(SszBytes32::get).toList();
  }

  public UInt64 getSlot() {
    return getField3().get();
  }

  public static BlockProofHistoricalSummariesContainer decodeBytes(Bytes bytes) {
    BlockProofHistoricalSummariesContainerSchema schema =
        BlockProofHistoricalSummariesContainerSchema.INSTANCE;
    BlockProofHistoricalSummariesContainer decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public static class BlockProofHistoricalSummariesContainerSchema
      extends ContainerSchema4<
          BlockProofHistoricalSummariesContainer,
          SszBytes32Vector,
          SszBytes32,
          SszList<SszBytes32>,
          SszUInt64> {

    public static final BlockProofHistoricalSummariesContainerSchema INSTANCE =
        new BlockProofHistoricalSummariesContainerSchema();

    private BlockProofHistoricalSummariesContainerSchema() {
      super(
          SszBeaconBlockProofHistoricalSummariesVector.getSchema(),
          SszPrimitiveSchemas.BYTES32_SCHEMA,
          SszExecutionBlockProofCapellaList.getSchema(),
          SszPrimitiveSchemas.UINT64_SCHEMA);
    }

    @Override
    public BlockProofHistoricalSummariesContainer createFromBackingNode(TreeNode node) {
      return new BlockProofHistoricalSummariesContainer(node);
    }
  }
}
