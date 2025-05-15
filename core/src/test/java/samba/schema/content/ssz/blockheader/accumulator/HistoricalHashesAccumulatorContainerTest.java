package samba.schema.content.ssz.blockheader.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoricalHashesAccumulatorContainerTest {

  List<Bytes32> historicalEpochs = new ArrayList<>();
  EpochRecordList epochRecordList;

  @BeforeEach
  public void setUp() {
    List<HeaderRecordContainer> headerRecordList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      headerRecordList.add(new HeaderRecordContainer(Bytes32.repeat((byte) i), UInt256.valueOf(i)));
      historicalEpochs.add(Bytes32.repeat((byte) i));
    }
    epochRecordList = new EpochRecordList(headerRecordList);
  }

  @Test
  public void testSszDecode() {
    HistoricalHashesAccumulatorContainer historicalHashesAccumulatorContainer =
        HistoricalHashesAccumulatorContainer.decodeBytes(
            Bytes.fromHexString(
                "0x0400000000000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404"));
    assertEquals(historicalHashesAccumulatorContainer.getHistoricalEpochs(), historicalEpochs);
  }

  @Test
  public void testSszEncode() {
    HistoricalHashesAccumulatorContainer historicalHashesAccumulatorContainer =
        new HistoricalHashesAccumulatorContainer(historicalEpochs);
    Bytes sszBytes = historicalHashesAccumulatorContainer.sszSerialize();
    assertEquals(
        sszBytes,
        Bytes.fromHexString(
            "0x0400000000000000000000000000000000000000000000000000000000000000000000000101010101010101010101010101010101010101010101010101010101010101020202020202020202020202020202020202020202020202020202020202020203030303030303030303030303030303030303030303030303030303030303030404040404040404040404040404040404040404040404040404040404040404"));
  }
}
