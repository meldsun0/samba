package samba.schema.content.ssz.blockheader;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBytes32VectorSchema;

public class SszBeaconBlockProofHistoricalRootsVector {

  private static final int BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE = 14;
  private static final SszBytes32VectorSchema<SszBytes32Vector> schema =
      SszBytes32VectorSchema.create(BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE);
  private final SszBytes32Vector BeaconBlockProofHistoricalRoots;

  public SszBeaconBlockProofHistoricalRootsVector(
      SszBytes32Vector BeaconBlockProofHistoricalRoots) {
    this.BeaconBlockProofHistoricalRoots = BeaconBlockProofHistoricalRoots;
  }

  public SszBeaconBlockProofHistoricalRootsVector(List<Bytes32> BeaconBlockProofHistoricalRoots) {
    if (BeaconBlockProofHistoricalRoots.size() != BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE) {
      throw new IllegalArgumentException(
          "BeaconBlockProofHistoricalRoots size is not equal to "
              + BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE);
    }
    this.BeaconBlockProofHistoricalRoots =
        schema.createFromElements(createSszBytes32List(BeaconBlockProofHistoricalRoots));
  }

  public SszBeaconBlockProofHistoricalRootsVector(Bytes BeaconBlockProofHistoricalRoots) {
    this.BeaconBlockProofHistoricalRoots = schema.sszDeserialize(BeaconBlockProofHistoricalRoots);
  }

  public static SszBytes32VectorSchema<SszBytes32Vector> getSchema() {
    return schema;
  }

  public static SszBytes32Vector createVector(List<Bytes32> BeaconBlockProofHistoricalRoots) {
    if (BeaconBlockProofHistoricalRoots.size() != BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE) {
      throw new IllegalArgumentException(
          "BeaconBlockProofHistoricalRoots size is not equal to "
              + BEACON_BLOCK_PROOF_HISTORICAL_ROOTS_VECTOR_SIZE);
    }
    return schema.createFromElements(createSszBytes32List(BeaconBlockProofHistoricalRoots));
  }

  public static List<Bytes32> decodeVector(SszBytes32Vector BeaconBlockProofHistoricalRoots) {
    return BeaconBlockProofHistoricalRoots.stream()
        .map(SszBytes32::get)
        .collect(Collectors.toList());
  }

  public SszBytes32Vector getEncodedVector() {
    return BeaconBlockProofHistoricalRoots;
  }

  public List<Bytes32> getDecodedVector() {
    return BeaconBlockProofHistoricalRoots.stream()
        .map(SszBytes32::get)
        .collect(Collectors.toList());
  }

  private static List<SszBytes32> createSszBytes32List(
      List<Bytes32> BeaconBlockProofHistoricalRoots) {
    return BeaconBlockProofHistoricalRoots.stream()
        .map(SszBytes32::of)
        .collect(Collectors.toList());
  }

  public Bytes sszSerialize() {
    return BeaconBlockProofHistoricalRoots.sszSerialize();
  }
}
