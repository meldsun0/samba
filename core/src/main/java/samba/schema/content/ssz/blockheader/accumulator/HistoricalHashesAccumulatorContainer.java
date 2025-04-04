package samba.schema.content.ssz.blockheader.accumulator;

import samba.validation.HistoricalHashesAccumulator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class HistoricalHashesAccumulatorContainer
    extends Container2<
        HistoricalHashesAccumulatorContainer, SszList<SszBytes32>, SszList<HeaderRecordContainer>> {

  public HistoricalHashesAccumulatorContainer(
      List<Bytes32> historicalEpochs, List<HeaderRecordContainer> headerRecords) {
    super(
        HistoricalHashesAccumulatorContainerSchema.INSTANCE,
        SszListSchema.create(
                SszPrimitiveSchemas.BYTES32_SCHEMA,
                HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS)
            .createFromElements(createSszBytes32List(historicalEpochs)),
        EpochRecordList.createList(headerRecords));
  }

  public HistoricalHashesAccumulatorContainer(
      List<Bytes32> historicalEpochs, EpochRecordList headerRecords) {
    super(
        HistoricalHashesAccumulatorContainerSchema.INSTANCE,
        SszListSchema.create(
                SszPrimitiveSchemas.BYTES32_SCHEMA,
                HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS)
            .createFromElements(createSszBytes32List(historicalEpochs)),
        headerRecords.getEncodedList());
  }

  public HistoricalHashesAccumulatorContainer(TreeNode backingNode) {
    super(HistoricalHashesAccumulatorContainerSchema.INSTANCE, backingNode);
  }

  private static List<SszBytes32> createSszBytes32List(List<Bytes32> historicalEpochs) {
    return historicalEpochs.stream().map(SszBytes32::of).collect(Collectors.toList());
  }

  public List<Bytes32> getHistoricalEpochs() {
    return getField0().stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  public List<HeaderRecordContainer> getEpochRecord() {
    return getField1().stream().collect(Collectors.toList());
  }

  public static HistoricalHashesAccumulatorContainer decodeBytes(Bytes sszBytes) {
    return HistoricalHashesAccumulatorContainerSchema.INSTANCE.sszDeserialize(sszBytes);
  }

  public static class HistoricalHashesAccumulatorContainerSchema
      extends ContainerSchema2<
          HistoricalHashesAccumulatorContainer,
          SszList<SszBytes32>,
          SszList<HeaderRecordContainer>> {
    public static final HistoricalHashesAccumulatorContainerSchema INSTANCE =
        new HistoricalHashesAccumulatorContainerSchema();

    private HistoricalHashesAccumulatorContainerSchema() {
      super(
          (SszListSchema<SszBytes32, SszList<SszBytes32>>)
              SszListSchema.create(
                  SszPrimitiveSchemas.BYTES32_SCHEMA,
                  HistoricalHashesAccumulator.MAX_HISTORICAL_EPOCHS),
          EpochRecordList.getSchema());
    }

    @Override
    public HistoricalHashesAccumulatorContainer createFromBackingNode(TreeNode backingNode) {
      return new HistoricalHashesAccumulatorContainer(backingNode);
    }
  }
}
