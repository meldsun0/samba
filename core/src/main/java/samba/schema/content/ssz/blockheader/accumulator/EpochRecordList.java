package samba.schema.content.ssz.blockheader.accumulator;

import samba.validation.HistoricalHashesAccumulator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;

public class EpochRecordList {

  private static final SszListSchema<HeaderRecordContainer, SszList<HeaderRecordContainer>> schema =
      (SszListSchema<HeaderRecordContainer, SszList<HeaderRecordContainer>>)
          SszListSchema.create(
              HeaderRecordContainer.HeaderRecordContainerSchema.INSTANCE,
              HistoricalHashesAccumulator.EPOCH_SIZE);

  private final SszList<HeaderRecordContainer> headerRecordList;

  public EpochRecordList(SszList<HeaderRecordContainer> headerRecordList) {
    if (headerRecordList.size() >= HistoricalHashesAccumulator.EPOCH_SIZE) {
      throw new IllegalArgumentException(
          "HeaderRecordList size is not equal to " + HistoricalHashesAccumulator.EPOCH_SIZE);
    }
    this.headerRecordList = headerRecordList;
  }

  public EpochRecordList(List<HeaderRecordContainer> headerRecordList) {
    if (headerRecordList.size() >= HistoricalHashesAccumulator.EPOCH_SIZE) {
      throw new IllegalArgumentException(
          "HeaderRecordList size is not equal to " + HistoricalHashesAccumulator.EPOCH_SIZE);
    }
    this.headerRecordList = schema.createFromElements(headerRecordList);
  }

  public EpochRecordList(Bytes epochRecordList) {
    this.headerRecordList = schema.sszDeserialize(epochRecordList);
  }

  public static SszListSchema<HeaderRecordContainer, SszList<HeaderRecordContainer>> getSchema() {
    return schema;
  }

  public static SszList<HeaderRecordContainer> createList(
      List<HeaderRecordContainer> headerRecordList) {
    if (headerRecordList.size() >= HistoricalHashesAccumulator.EPOCH_SIZE) {
      throw new IllegalArgumentException(
          "HeaderRecordList size is not equal to " + HistoricalHashesAccumulator.EPOCH_SIZE);
    }
    return schema.createFromElements(headerRecordList);
  }

  public static List<HeaderRecordContainer> decodeList(
      SszList<HeaderRecordContainer> headerRecordList) {
    if (headerRecordList.size() >= HistoricalHashesAccumulator.EPOCH_SIZE) {
      throw new IllegalArgumentException(
          "HeaderRecordList size is not equal to " + HistoricalHashesAccumulator.EPOCH_SIZE);
    }
    return headerRecordList.stream().collect(Collectors.toList());
  }

  public SszList<HeaderRecordContainer> getEncodedList() {
    return headerRecordList;
  }

  public List<HeaderRecordContainer> getDecodedList() {
    return headerRecordList.stream().collect(Collectors.toList());
  }

  public Bytes sszSerialize() {
    return headerRecordList.sszSerialize();
  }
}
