package samba.schema.content.ssz.blockheader;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBytes32Vector;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBytes32VectorSchema;

public class SszExecutionBlockProofVector {

  private static final int EXECUTION_BLOCK_PROOF_SIZE = 11;
  private static final SszBytes32VectorSchema<SszBytes32Vector> schema =
      SszBytes32VectorSchema.create(EXECUTION_BLOCK_PROOF_SIZE);
  private final SszBytes32Vector ExecutionBlockProof;

  public SszExecutionBlockProofVector(SszBytes32Vector ExecutionBlockProof) {
    this.ExecutionBlockProof = ExecutionBlockProof;
  }

  public SszExecutionBlockProofVector(List<Bytes32> ExecutionBlockProof) {
    this.ExecutionBlockProof = schema.createFromElements(createSszBytes32List(ExecutionBlockProof));
  }

  public static SszBytes32VectorSchema<SszBytes32Vector> getSchema() {
    return schema;
  }

  public static SszBytes32Vector createVector(List<Bytes32> ExecutionBlockProof) {
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
}
