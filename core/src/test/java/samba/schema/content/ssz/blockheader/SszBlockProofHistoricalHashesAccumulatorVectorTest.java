package samba.schema.content.ssz.blockheader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.content.ContentProofConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszBlockProofHistoricalHashesAccumulatorVectorTest {

  List<Bytes32> blockProofHistoricalHashesAccumulator;

  @BeforeEach
  public void setup() {
    this.blockProofHistoricalHashesAccumulator = new ArrayList<>();
    for (int i = 0;
        i < ContentProofConstants.BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR_VECTOR_SIZE;
        i++) this.blockProofHistoricalHashesAccumulator.add(Bytes32.repeat((byte) i));
  }

  @Test
  public void testSszDecode() {
    SszBlockProofHistoricalHashesAccumulatorVector sszBlockProofHistoricalHashesAccumulatorVector =
        new SszBlockProofHistoricalHashesAccumulatorVector(
            Bytes.fromHexString(
                "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404050505050505050505050505050505050505050505050505050505050505050506060606060606060606060606060606060606060606060606060606060606060707070707070707070707070707070707070707070707070707070707070707080808080808080808080808080808080808080808080808080808080808080809090909090909090909090909090909090909090909090909090909090909090a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e"));
    List<Bytes32> decodedBlockProofHistoricalHashesAccumulator =
        sszBlockProofHistoricalHashesAccumulatorVector.getDecodedVector();
    assertEquals(
        this.blockProofHistoricalHashesAccumulator, decodedBlockProofHistoricalHashesAccumulator);
  }

  @Test
  public void testSszEncode() {
    SszBlockProofHistoricalHashesAccumulatorVector sszBlockProofHistoricalHashesAccumulatorVector =
        new SszBlockProofHistoricalHashesAccumulatorVector(
            this.blockProofHistoricalHashesAccumulator);
    Bytes encodedBlockProofHistoricalHashesAccumulator =
        sszBlockProofHistoricalHashesAccumulatorVector.sszSerialize();
    assertEquals(
        encodedBlockProofHistoricalHashesAccumulator,
        Bytes.fromHexString(
            "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404050505050505050505050505050505050505050505050505050505050505050506060606060606060606060606060606060606060606060606060606060606060707070707070707070707070707070707070707070707070707070707070707080808080808080808080808080808080808080808080808080808080808080809090909090909090909090909090909090909090909090909090909090909090a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0d0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e0e"));
  }
}
