package samba.schema.content.ssz.blockheader;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBytes32VectorSchema;

public class SszBeaconBlockProofHistoricalSummariesVector {

  private static final int BEACON_BLOCK_PROOF_HISTORICAL_SUMMARIES_VECTOR_SIZE = 13;
  private static final SszBytes32VectorSchema<SszBytes32Vector> schema =
      SszBytes32VectorSchema.create(BEACON_BLOCK_PROOF_HISTORICAL_SUMMARIES_VECTOR_SIZE);
  private final SszBytes32Vector BeaconBlockProofHistoricalSummaries;

  public SszBeaconBlockProofHistoricalSummariesVector(
      SszBytes32Vector BeaconBlockProofHistoricalSummaries) {
    this.BeaconBlockProofHistoricalSummaries = BeaconBlockProofHistoricalSummaries;
  }

  public SszBeaconBlockProofHistoricalSummariesVector(
      List<Bytes32> BeaconBlockProofHistoricalSummaries) {
    this.BeaconBlockProofHistoricalSummaries =
        schema.createFromElements(createSszBytes32List(BeaconBlockProofHistoricalSummaries));
  }

  public static SszBytes32VectorSchema<SszBytes32Vector> getSchema() {
    return schema;
  }

  public static SszBytes32Vector createVector(List<Bytes32> BeaconBlockProofHistoricalSummaries) {
    return schema.createFromElements(createSszBytes32List(BeaconBlockProofHistoricalSummaries));
  }

  public static List<Bytes32> decodeVector(SszBytes32Vector BeaconBlockProofHistoricalSummaries) {
    return BeaconBlockProofHistoricalSummaries.stream()
        .map(SszBytes32::get)
        .collect(Collectors.toList());
  }

  public SszBytes32Vector getEncodedVector() {
    return BeaconBlockProofHistoricalSummaries;
  }

  public List<Bytes32> getDecodedVector() {
    return BeaconBlockProofHistoricalSummaries.stream()
        .map(SszBytes32::get)
        .collect(Collectors.toList());
  }

  private static List<SszBytes32> createSszBytes32List(
      List<Bytes32> BeaconBlockProofHistoricalSummaries) {
    return BeaconBlockProofHistoricalSummaries.stream()
        .map(SszBytes32::of)
        .collect(Collectors.toList());
  }
}
