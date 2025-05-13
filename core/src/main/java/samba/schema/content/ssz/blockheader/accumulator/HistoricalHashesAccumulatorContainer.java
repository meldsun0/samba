package samba.schema.content.ssz.blockheader.accumulator;

import samba.validation.HistoricalHashesAccumulator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class HistoricalHashesAccumulatorContainer
    extends Container1<HistoricalHashesAccumulatorContainer, SszList<SszBytes32>> {

  public HistoricalHashesAccumulatorContainer(List<Bytes32> historicalEpochs) {
    super(
        HistoricalHashesAccumulatorContainerSchema.INSTANCE,
        SszListSchema.create(
                SszPrimitiveSchemas.BYTES32_SCHEMA,
                HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS)
            .createFromElements(createSszBytes32List(historicalEpochs)));
  }

  public HistoricalHashesAccumulatorContainer(TreeNode backingNode) {
    super(HistoricalHashesAccumulatorContainerSchema.INSTANCE, backingNode);
  }

  public HistoricalHashesAccumulatorContainer() {
    super(
        HistoricalHashesAccumulatorContainerSchema.INSTANCE,
        SszListSchema.create(
                SszPrimitiveSchemas.BYTES32_SCHEMA,
                HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS)
            .createFromElements(createSszBytes32List(List.of())));
  }

  private static List<SszBytes32> createSszBytes32List(List<Bytes32> historicalEpochs) {
    return historicalEpochs.stream().map(SszBytes32::of).collect(Collectors.toList());
  }

  public List<Bytes32> getHistoricalEpochs() {
    return getField0().stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  public SszList<SszBytes32> getHistoricalEpochsSsz() {
    return getField0();
  }

  public static HistoricalHashesAccumulatorContainer decodeBytes(Bytes sszBytes) {
    return HistoricalHashesAccumulatorContainerSchema.INSTANCE.sszDeserialize(sszBytes);
  }

  public static class HistoricalHashesAccumulatorContainerSchema
      extends ContainerSchema1<HistoricalHashesAccumulatorContainer, SszList<SszBytes32>> {
    public static final HistoricalHashesAccumulatorContainerSchema INSTANCE =
        new HistoricalHashesAccumulatorContainerSchema();

    private HistoricalHashesAccumulatorContainerSchema() {
      super(
          (SszListSchema<SszBytes32, SszList<SszBytes32>>)
              SszListSchema.create(
                  SszPrimitiveSchemas.BYTES32_SCHEMA,
                  HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS));
    }

    @Override
    public HistoricalHashesAccumulatorContainer createFromBackingNode(TreeNode backingNode) {
      return new HistoricalHashesAccumulatorContainer(backingNode);
    }
  }
}
