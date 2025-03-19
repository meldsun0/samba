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

public class BlockProofHistoricalRootsContainer
    extends Container4<
        BlockProofHistoricalRootsContainer,
        SszBytes32Vector,
        SszBytes32,
        SszBytes32Vector,
        SszUInt64> {

  public BlockProofHistoricalRootsContainer(
      final List<Bytes32> beaconBlockProofHistoricalRoots,
      final Bytes32 blockRoot,
      final List<Bytes32> executionBlockProof,
      final UInt64 slot) {
    super(
        BlockProofHistoricalRootsContainerSchema.INSTANCE,
        SszBeaconBlockProofHistoricalRootsVector.createVector(beaconBlockProofHistoricalRoots),
        SszBytes32.of(blockRoot),
        SszExecutionBlockProofVector.createVector(executionBlockProof),
        SszUInt64.of(slot));
  }

  public BlockProofHistoricalRootsContainer(TreeNode backingNode) {
    super(BlockProofHistoricalRootsContainerSchema.INSTANCE, backingNode);
  }

  public List<Bytes32> getBeaconBlockProofHistoricalRoots() {
    return getField0().asListUnboxed();
  }

  public Bytes32 getBlockRoot() {
    return getField1().get();
  }

  public List<Bytes32> getExecutionBlockProof() {
    return getField2().asListUnboxed();
  }

  public UInt64 getSlot() {
    return getField3().get();
  }

  public static BlockProofHistoricalRootsContainer decodeBytes(Bytes bytes) {
    BlockProofHistoricalRootsContainerSchema schema =
        BlockProofHistoricalRootsContainerSchema.INSTANCE;
    BlockProofHistoricalRootsContainer decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public static class BlockProofHistoricalRootsContainerSchema
      extends ContainerSchema4<
          BlockProofHistoricalRootsContainer,
          SszBytes32Vector,
          SszBytes32,
          SszBytes32Vector,
          SszUInt64> {
    public static BlockProofHistoricalRootsContainerSchema INSTANCE =
        new BlockProofHistoricalRootsContainerSchema();

    private BlockProofHistoricalRootsContainerSchema() {
      super(
          SszBeaconBlockProofHistoricalRootsVector.getSchema(),
          SszPrimitiveSchemas.BYTES32_SCHEMA,
          SszExecutionBlockProofVector.getSchema(),
          SszPrimitiveSchemas.UINT64_SCHEMA);
    }

    @Override
    public BlockProofHistoricalRootsContainer createFromBackingNode(TreeNode node) {
      return new BlockProofHistoricalRootsContainer(node);
    }
  }
}
