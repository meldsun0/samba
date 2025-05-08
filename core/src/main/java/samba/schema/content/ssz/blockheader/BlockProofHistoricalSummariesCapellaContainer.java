package samba.schema.content.ssz.blockheader;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.containers.Container4;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema4;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class BlockProofHistoricalSummariesCapellaContainer
    extends Container4<
        BlockProofHistoricalSummariesCapellaContainer,
        SszBytes32Vector,
        SszBytes32,
        SszBytes32Vector,
        SszUInt64> {

  public BlockProofHistoricalSummariesCapellaContainer(
      final List<Bytes32> beaconBlockProofHistoricalSummaries,
      final Bytes32 blockRoot,
      final List<Bytes32> executionBlockProof,
      final UInt64 slot) {
    super(
        BlockProofHistoricalSummariesContainerSchema.INSTANCE,
        SszBeaconBlockProofHistoricalSummariesVector.createVector(
            beaconBlockProofHistoricalSummaries),
        SszBytes32.of(blockRoot),
        SszExecutionBlockProofBellatrixVector.createVector(executionBlockProof),
        SszUInt64.of(slot));
  }

  public BlockProofHistoricalSummariesCapellaContainer(TreeNode backingNode) {
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

  public static BlockProofHistoricalSummariesCapellaContainer decodeBytes(Bytes bytes) {
    BlockProofHistoricalSummariesContainerSchema schema =
        BlockProofHistoricalSummariesContainerSchema.INSTANCE;
    BlockProofHistoricalSummariesCapellaContainer decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public static class BlockProofHistoricalSummariesContainerSchema
      extends ContainerSchema4<
          BlockProofHistoricalSummariesCapellaContainer,
          SszBytes32Vector,
          SszBytes32,
          SszBytes32Vector,
          SszUInt64> {

    public static final BlockProofHistoricalSummariesContainerSchema INSTANCE =
        new BlockProofHistoricalSummariesContainerSchema();

    private BlockProofHistoricalSummariesContainerSchema() {
      super(
          SszBeaconBlockProofHistoricalSummariesVector.getSchema(),
          SszPrimitiveSchemas.BYTES32_SCHEMA,
          SszExecutionBlockProofBellatrixVector.getSchema(),
          SszPrimitiveSchemas.UINT64_SCHEMA);
    }

    @Override
    public BlockProofHistoricalSummariesCapellaContainer createFromBackingNode(TreeNode node) {
      return new BlockProofHistoricalSummariesCapellaContainer(node);
    }
  }
}
