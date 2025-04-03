package samba.schema.content.ssz.blockheader.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

public class HeaderRecordContainerTest {

  Bytes32 blockHash = Bytes32.repeat((byte) 0x01);
  UInt256 totalDifficulty = UInt256.valueOf(1);

  @Test
  public void testSszDecode() {
    HeaderRecordContainer headerRecordContainer =
        HeaderRecordContainer.decodeBytes(
            Bytes.fromHexString(
                "0x01010101010101010101010101010101010101010101010101010101010101010100000000000000000000000000000000000000000000000000000000000000"));
    assertEquals(headerRecordContainer.getBlockHash(), blockHash);
    assertEquals(headerRecordContainer.getTotalDifficulty(), totalDifficulty);
  }

  @Test
  public void testSszEncode() {
    HeaderRecordContainer headerRecordContainer =
        new HeaderRecordContainer(blockHash, totalDifficulty);
    Bytes sszBytes = headerRecordContainer.sszSerialize();
    assertEquals(
        sszBytes,
        Bytes.fromHexString(
            "0x01010101010101010101010101010101010101010101010101010101010101010100000000000000000000000000000000000000000000000000000000000000"));
  }
}
