package samba.schema.content.ssz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ContentKeyBlockNumberContainerTest {

  UInt64 blockNumber = UInt64.valueOf(1234);

  @Test
  public void testSszDecode() {
    ContentKeyBlockNumberContainer contentKeyBlockNumberContainer =
        ContentKeyBlockNumberContainer.decodeBytes(Bytes.fromHexString("0xd204000000000000"));
    UInt64 decodedBlockNumber = contentKeyBlockNumberContainer.getBlockNumber();
    assertEquals(this.blockNumber, decodedBlockNumber);
  }

  @Test
  public void testSszEncode() {
    ContentKeyBlockNumberContainer contentKeyBlockNumberContainer =
        new ContentKeyBlockNumberContainer(this.blockNumber);
    Bytes encodedBytes = contentKeyBlockNumberContainer.sszSerialize();
    assertEquals(encodedBytes, Bytes.fromHexString("0xd204000000000000"));
  }
}
