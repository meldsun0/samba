package samba.schema.content.ssz.blockheader;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszPrimitiveListSchema;

public class SszExecutionBlockProofCapellaList {

  private static final int EXECUTION_BLOCK_PROOF_LIMIT = 12;
  // TODO
  private static final SszListSchema<SszBytes32, SszList<SszBytes32>> schema =
      (SszListSchema<SszBytes32, SszList<SszBytes32>>)
          SszPrimitiveListSchema.create(
              SszPrimitiveSchemas.BYTES32_SCHEMA, EXECUTION_BLOCK_PROOF_LIMIT);
  private final SszList<SszBytes32> ExecutionBlockProof;

  public SszExecutionBlockProofCapellaList(SszList<SszBytes32> ExecutionBlockProof) {
    this.ExecutionBlockProof = ExecutionBlockProof;
  }

  public SszExecutionBlockProofCapellaList(List<Bytes32> ExecutionBlockProof) {
    this.ExecutionBlockProof = schema.createFromElements(createSszBytes32List(ExecutionBlockProof));
  }

  public static SszListSchema<SszBytes32, SszList<SszBytes32>> getSchema() {
    return schema;
  }

  public static SszList<SszBytes32> createList(List<Bytes32> ExecutionBlockProof) {
    return schema.createFromElements(createSszBytes32List(ExecutionBlockProof));
  }

  public static List<Bytes32> decodeList(SszList<SszBytes32> ExecutionBlockProof) {
    return ExecutionBlockProof.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  public SszList<SszBytes32> getEncodedList() {
    return ExecutionBlockProof;
  }

  public List<Bytes32> getDecodedList() {
    return ExecutionBlockProof.stream().map(SszBytes32::get).collect(Collectors.toList());
  }

  private static List<SszBytes32> createSszBytes32List(List<Bytes32> ExecutionBlockProof) {
    return ExecutionBlockProof.stream().map(SszBytes32::of).collect(Collectors.toList());
  }
}
