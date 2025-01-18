package samba.schema.content.ssz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class ContentKeyBlockHashContainerTest {

  Bytes32 blockHash;

  public ContentKeyBlockHashContainerTest() {
    this.blockHash = Bytes32.repeat((byte) 1);
  }

  @Test
  public void testSszDecode() {
    ContentKeyBlockHashContainer container =
        ContentKeyBlockHashContainer.decodeBytes(
            Bytes.fromHexString(
                "0x0101010101010101010101010101010101010101010101010101010101010101"));
    Bytes32 decodedBlockHash = container.getBlockHash();
    assertEquals(this.blockHash, decodedBlockHash);
  }

  @Test
  public void testSszEncode() {
    ContentKeyBlockHashContainer container = new ContentKeyBlockHashContainer(this.blockHash);
    Bytes encodedBytes = container.sszSerialize();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString("0x0101010101010101010101010101010101010101010101010101010101010101"));
  }
}
