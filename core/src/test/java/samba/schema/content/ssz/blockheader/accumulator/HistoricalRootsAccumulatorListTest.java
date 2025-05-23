package samba.schema.content.ssz.blockheader.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoricalRootsAccumulatorListTest {

  List<Bytes32> historicalRoots;

  @BeforeEach
  public void setUp() {
    historicalRoots = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      historicalRoots.add(Bytes32.repeat((byte) i));
    }
  }

  @Test
  public void testSszDecode() {
    HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
        HistoricalRootsAccumulatorList.decodeBytes(
            Bytes.fromHexString(
                "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404"));
    assertEquals(historicalRootsAccumulatorList.getDecodedList(), historicalRoots);
  }

  @Test
  public void testSszEncode() {
    HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
        new HistoricalRootsAccumulatorList(historicalRoots);
    Bytes encodedHistoricalRoots = historicalRootsAccumulatorList.sszSerialize();
    assertEquals(
        encodedHistoricalRoots,
        Bytes.fromHexString(
            "0x00000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404"));
  }
}
