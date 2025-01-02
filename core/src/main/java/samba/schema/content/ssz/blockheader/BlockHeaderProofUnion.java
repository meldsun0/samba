package samba.schema.content.ssz.blockheader;

import samba.domain.content.ContentProofType;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszUnion;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.SszUnionSchema;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class BlockHeaderProofUnion {

  private static SszUnionSchema<SszUnion> schema =
      SszUnionSchema.create(
          SszPrimitiveSchemas.NONE_SCHEMA,
          SszBlockProofHistoricalHashesAccumulatorVector.getSchema(),
          BlockProofHistoricalRootsContainer.BlockProofHistoricalRootsContainerSchema.INSTANCE,
          BlockProofHistoricalSummariesContainer.BlockProofHistoricalSummariesContainerSchema
              .INSTANCE);
  private final SszUnion union;

  public BlockHeaderProofUnion(SszUnion union) {
    this.union = union;
  }

  public BlockHeaderProofUnion(Byte proofType) {
    this.union = schema.createFromValue(proofType.intValue(), null);
  }

  public BlockHeaderProofUnion(
      ContentProofType proofType, List<Bytes32> blockProofHistoricalHashesAccumulator) {
    this.union =
        schema.createFromValue(
            proofType.getValue(),
            SszBlockProofHistoricalHashesAccumulatorVector.createVector(
                blockProofHistoricalHashesAccumulator));
  }

  public BlockHeaderProofUnion(
      ContentProofType proofType,
      List<Bytes32> beaconBlockProof,
      Bytes32 beaconBlockRoot,
      List<Bytes32> executionBlockProof,
      UInt64 slot) {
    this.union =
        schema.createFromValue(
            proofType.getValue(),
            new BlockProofHistoricalSummariesContainer(
                beaconBlockProof, beaconBlockRoot, executionBlockProof, slot));
  }

  public ContentProofType getProofType() {
    return ContentProofType.fromValue(union.getSelector());
  }

  public List<Bytes32> getBlockProofHistoricalHashesAccumulator() {
    if (union.getSelector() == 1) {
      SszBytes32Vector blockProofHistoricalHashesAccumulator = (SszBytes32Vector) union.getValue();
      return SszBlockProofHistoricalHashesAccumulatorVector.decodeVector(
          blockProofHistoricalHashesAccumulator);
    }
    return List.of();
  }

  public BlockProofHistoricalRootsContainer getBlockProofHistoricalRootsContainer() {
    if (union.getSelector() == 2) {
      return (BlockProofHistoricalRootsContainer) union.getValue();
    }
    return null;
  }

  public BlockProofHistoricalSummariesContainer getBlockProofHistoricalSummariesContainer() {
    if (union.getSelector() == 3) {
      return (BlockProofHistoricalSummariesContainer) union.getValue();
    }
    return null;
  }

  public SszUnion getUnion() {
    return union;
  }

  public static SszUnionSchema<SszUnion> getSchema() {
    return schema;
  }

  public static SszUnion decodeBytes(Bytes bytes) {
    SszUnion decodedBytes = schema.sszDeserialize(bytes);
    return decodedBytes;
  }

  public Bytes sszSerialize() {
    return union.sszSerialize();
  }
}
