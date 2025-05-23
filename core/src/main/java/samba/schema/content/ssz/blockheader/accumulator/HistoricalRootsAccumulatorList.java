package samba.schema.content.ssz.blockheader.accumulator;

import samba.validation.HistoricalRootsAccumulator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;

public class HistoricalRootsAccumulatorList {

  private static final SszListSchema<SszBytes32, SszList<SszBytes32>> schema =
      (SszListSchema<SszBytes32, SszList<SszBytes32>>)
          SszListSchema.create(
              SszPrimitiveSchemas.BYTES32_SCHEMA,
              HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT);

  private final SszList<SszBytes32> historicalRoots;

  public HistoricalRootsAccumulatorList(SszList<SszBytes32> historicalRoots) {
    if (historicalRoots.size() > HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT) {
      throw new IllegalArgumentException(
          "historicalRoots size is not equal to "
              + HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT);
    }
    this.historicalRoots = historicalRoots;
  }

  public HistoricalRootsAccumulatorList(List<Bytes32> historicalRoots) {
    if (historicalRoots.size() > HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT) {
      throw new IllegalArgumentException(
          "historicalRoots size is not equal to "
              + HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT);
    }
    this.historicalRoots = schema.createFromElements(createSszBytes32List(historicalRoots));
  }

  public HistoricalRootsAccumulatorList(Bytes historicalRoots) {
    this.historicalRoots = schema.sszDeserialize(historicalRoots);
  }

  public static HistoricalRootsAccumulatorList decodeBytes(Bytes sszBytes) {
    return new HistoricalRootsAccumulatorList(schema.sszDeserialize(sszBytes));
  }

  public static SszListSchema<SszBytes32, SszList<SszBytes32>> getSchema() {
    return schema;
  }

  public static SszList<SszBytes32> createList(List<Bytes32> historicalRoots) {
    if (historicalRoots.size() > HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT) {
      throw new IllegalArgumentException(
          "historicalRoots size is not equal to "
              + HistoricalRootsAccumulator.HISTORICAL_ROOTS_LIMIT);
    }
    return schema.createFromElements(createSszBytes32List(historicalRoots));
  }

  public static List<Bytes32> decodeList(SszList<SszBytes32> historicalRoots) {
    return historicalRoots.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  public SszList<SszBytes32> getEncodedList() {
    return historicalRoots;
  }

  public List<Bytes32> getDecodedList() {
    return historicalRoots.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  private static List<SszBytes32> createSszBytes32List(List<Bytes32> historicalRoots) {
    return historicalRoots.stream().map(SszBytes32::of).collect(Collectors.toList());
  }

  public Bytes sszSerialize() {
    return historicalRoots.sszSerialize();
  }
}
