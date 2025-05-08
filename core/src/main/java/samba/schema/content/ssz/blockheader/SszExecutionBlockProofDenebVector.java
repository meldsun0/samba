package samba.schema.content.ssz.blockheader;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBytes32VectorSchema;

public class SszExecutionBlockProofDenebVector {

  private static final int EXECUTION_BLOCK_PROOF_SIZE = 12;
  private static final SszBytes32VectorSchema<SszBytes32Vector> schema =
      SszBytes32VectorSchema.create(EXECUTION_BLOCK_PROOF_SIZE);
  private final SszBytes32Vector ExecutionBlockProof;

  public SszExecutionBlockProofDenebVector(SszBytes32Vector ExecutionBlockProof) {
    this.ExecutionBlockProof = ExecutionBlockProof;
  }

  public SszExecutionBlockProofDenebVector(List<Bytes32> ExecutionBlockProof) {
    if (ExecutionBlockProof.size() != EXECUTION_BLOCK_PROOF_SIZE) {
      throw new IllegalArgumentException(
          "ExecutionBlockProof size is not equal to " + EXECUTION_BLOCK_PROOF_SIZE);
    }
    this.ExecutionBlockProof = schema.createFromElements(createSszBytes32List(ExecutionBlockProof));
  }

  public SszExecutionBlockProofDenebVector(Bytes ExecutionBlockProof) {
    this.ExecutionBlockProof = schema.sszDeserialize(ExecutionBlockProof);
  }

  public static SszBytes32VectorSchema<SszBytes32Vector> getSchema() {
    return schema;
  }

  public static SszBytes32Vector createVector(List<Bytes32> ExecutionBlockProof) {
    if (ExecutionBlockProof.size() != EXECUTION_BLOCK_PROOF_SIZE) {
      throw new IllegalArgumentException(
          "ExecutionBlockProof size is not equal to " + EXECUTION_BLOCK_PROOF_SIZE);
    }
    return schema.createFromElements(createSszBytes32List(ExecutionBlockProof));
  }

  public static List<Bytes32> decodeVector(SszBytes32Vector ExecutionBlockProof) {
    return ExecutionBlockProof.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  public SszBytes32Vector getEncodedVector() {
    return ExecutionBlockProof;
  }

  public List<Bytes32> getDecodedVector() {
    return ExecutionBlockProof.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  private static List<SszBytes32> createSszBytes32List(List<Bytes32> ExecutionBlockProof) {
    return ExecutionBlockProof.stream().map(SszBytes32::of).collect(Collectors.toList());
  }

  public Bytes sszSerialize() {
    return ExecutionBlockProof.sszSerialize();
  }
}
