package samba.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class UtilTest {

  @Test
  public void testLeb128EncodingDecoding() {
    int value1 = 300;
    Bytes encoded1 = Util.writeUnsignedLeb128(value1);
    int decoded1 = Util.readUnsignedLeb128(encoded1);
    assertEquals(value1, decoded1, "Decoded value should match original value");

    int value2 = 127;
    Bytes encoded2 = Util.writeUnsignedLeb128(value2);
    int decoded2 = Util.readUnsignedLeb128(encoded2);
    assertEquals(value2, decoded2, "Decoded value should match original value");

    int value3 = 16256;
    Bytes encoded3 = Util.writeUnsignedLeb128(value3);
    int decoded3 = Util.readUnsignedLeb128(encoded3);
    assertEquals(value3, decoded3, "Decoded value should match original value");

    int value4 = 0;
    Bytes encoded4 = Util.writeUnsignedLeb128(value4);
    assertEquals(1, encoded4.size(), "Must be 1 byte");
    int decoded4 = Util.readUnsignedLeb128(encoded4);
    assertEquals(value4, decoded4, "Decoded value should match original value");
  }

  @Test
  public void testEdgeCases() {
    int value = 127;
    Bytes encoded = Util.writeUnsignedLeb128(value);
    int decoded = Util.readUnsignedLeb128(encoded);
    assertEquals(value, decoded, "Decoded value should match original value for 127");

    value = 128;
    encoded = Util.writeUnsignedLeb128(value);
    decoded = Util.readUnsignedLeb128(encoded);
    assertEquals(value, decoded, "Decoded value should match original value for 128");

    value = 0x7FFFFFFF; // Largest 32-bit unsigned value
    encoded = Util.writeUnsignedLeb128(value);
    decoded = Util.readUnsignedLeb128(encoded);
    assertEquals(value, decoded, "Decoded value should match original value for 0x7FFFFFFF");
  }
}
