package samba.schema.content.ssz.blockheader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.content.ContentProofConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszBeaconBlockProofHistoricalSummariesVectorTest {

  List<Bytes32> beaconBlockProofHistoricalSummaries;

  @BeforeEach
  public void setup() {
    this.beaconBlockProofHistoricalSummaries = new ArrayList<>();
    for (int i = 0;
        i < ContentProofConstants.BEACON_BLOCK_PROOF_HISTORICAL_SUMMARIES_VECTOR_SIZE;
        i++) this.beaconBlockProofHistoricalSummaries.add(Bytes32.repeat((byte) i));
  }

  @Test
  public void testSszDecode() {
    SszBeaconBlockProofHistoricalSummariesVector sszBeaconBlockProofHistoricalSummariesVector =
        new SszBeaconBlockProofHistoricalSummariesVector(
            Bytes.fromHexString(
                "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404050505050505050505050505050505050505050505050505050505050505050506060606060606060606060606060606060606060606060606060606060606060707070707070707070707070707070707070707070707070707070707070707080808080808080808080808080808080808080808080808080808080808080809090909090909090909090909090909090909090909090909090909090909090a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c"));
    List<Bytes32> decodedBeaconBlockProofHistoricalSummaries =
        sszBeaconBlockProofHistoricalSummariesVector.getDecodedVector();
    assertEquals(
        this.beaconBlockProofHistoricalSummaries, decodedBeaconBlockProofHistoricalSummaries);
  }

  @Test
  public void testSszEncode() {
    SszBeaconBlockProofHistoricalSummariesVector sszBeaconBlockProofHistoricalSummariesVector =
        new SszBeaconBlockProofHistoricalSummariesVector(this.beaconBlockProofHistoricalSummaries);
    Bytes encodedBeaconBlockProofHistoricalSummaries =
        sszBeaconBlockProofHistoricalSummariesVector.sszSerialize();
    assertEquals(
        encodedBeaconBlockProofHistoricalSummaries,
        Bytes.fromHexString(
            "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404050505050505050505050505050505050505050505050505050505050505050506060606060606060606060606060606060606060606060606060606060606060707070707070707070707070707070707070707070707070707070707070707080808080808080808080808080808080808080808080808080808080808080809090909090909090909090909090909090909090909090909090909090909090a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c"));
  }
}
