package samba.schema.content.ssz.blockbody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszUnclesByteListTest {

  List<BlockHeader> uncles;

  @BeforeEach
  public void setup() {
    this.uncles = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      this.uncles.add(BlockHeaderBuilder.createDefault().buildBlockHeader());
    }
  }

  @Test
  public void testSszDecode() {
    SszUnclesByteList sszUnclesByteList =
        new SszUnclesByteList(
            Bytes.fromHexString(
                "0xf905dcf901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000f901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000f901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000"));
    List<BlockHeader> decodedUncles = sszUnclesByteList.getDecodedUncles();
    for (int i = 0; i < 3; i++) {
      assertTrue(this.uncles.get(i).equals(decodedUncles.get(i)));
    }
  }

  @Test
  public void testSszDecodeEmptyList() {
    SszUnclesByteList sszUnclesByteList = new SszUnclesByteList(List.of());
    List<BlockHeader> decodedUncles = sszUnclesByteList.getDecodedUncles();
    assertTrue(decodedUncles.isEmpty());
  }

  @Test
  public void testSszDecodeEmptyBytes() {
    SszUnclesByteList sszUnclesByteList =
        new SszUnclesByteList(Bytes.fromHexString("0xc0")); // RLP empty list
    List<BlockHeader> decodedUncles = sszUnclesByteList.getDecodedUncles();
    assertTrue(decodedUncles.isEmpty());
  }

  @Test
  public void testSszEncode() {
    SszUnclesByteList sszUnclesByteList = new SszUnclesByteList(this.uncles);
    Bytes encodedUncles = sszUnclesByteList.sszSerialize();
    assertEquals(
        encodedUncles,
        Bytes.fromHexString(
            "0xf905dcf901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000f901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000f901f1a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001808401c9c380808080a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470880000000000000000"));
  }

  @Test
  public void testSszEncodeEmptyList() {
    SszUnclesByteList sszUnclesByteList = new SszUnclesByteList(List.of());
    Bytes encodedUncles = sszUnclesByteList.sszSerialize();
    assertEquals(encodedUncles, Bytes.fromHexString("0xc0"));
  }

  @Test
  public void testSszEncodeEmptyBytes() {
    SszUnclesByteList sszUnclesByteList =
        new SszUnclesByteList(Bytes.fromHexString("0xc0")); // RLP empty list
    Bytes encodedUncles = sszUnclesByteList.sszSerialize();
    assertEquals(encodedUncles, Bytes.fromHexString("0xc0"));
  }
}
