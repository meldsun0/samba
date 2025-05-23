package samba.validation;

import static org.junit.jupiter.api.Assertions.fail;

import samba.schema.content.ssz.blockheader.accumulator.HistoricalRootsAccumulatorList;

import java.io.InputStream;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class HistoricalRootsAccumulatorTest {

  @Test
  public void testValidateValidBlockHeader() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("historicalroots.bin");

      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
          HistoricalRootsAccumulatorList.decodeBytes(accumulatorBytes);
    } catch (Exception e) {
      fail("Failed to read historical roots accumulator file: " + e.getMessage());
    }
  }
}
