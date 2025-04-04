package samba.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class UtilTest {

//  @Test
//  public void testLeb128EncodingDecoding() {
//    int value1 = 300;
//    Bytes encoded1 = Util.writeUnsignedLeb128(value1);
//    int decoded1 = Util.readUnsignedLeb128(encoded1);
//    assertEquals(value1, decoded1, "Decoded value should match original value");
//
//    int value2 = 127;
//    Bytes encoded2 = Util.writeUnsignedLeb128(value2);
//    int decoded2 = Util.readUnsignedLeb128(encoded2);
//    assertEquals(value2, decoded2, "Decoded value should match original value");
//
//    int value3 = 16256;
//    Bytes encoded3 = Util.writeUnsignedLeb128(value3);
//    int decoded3 = Util.readUnsignedLeb128(encoded3);
//    assertEquals(value3, decoded3, "Decoded value should match original value");
//
//    int value4 = 0;
//    Bytes encoded4 = Util.writeUnsignedLeb128(value4);
//    assertEquals(1, encoded4.size(), "Must be 1 byte");
//    int decoded4 = Util.readUnsignedLeb128(encoded4);
//    assertEquals(value4, decoded4, "Decoded value should match original value");
//  }

//  @Test
//  public void testEdgeCases() {
//    int value = 127;
//    Bytes encoded = Util.writeUnsignedLeb128(value);
//    int decoded = Util.readUnsignedLeb128(encoded);
//    assertEquals(value, decoded, "Decoded value should match original value for 127");
//
//    value = 128;
//    encoded = Util.writeUnsignedLeb128(value);
//    decoded = Util.readUnsignedLeb128(encoded);
//    assertEquals(value, decoded, "Decoded value should match original value for 128");
//
//    value = 0x7FFFFFFF; // Largest 32-bit unsigned value
//    encoded = Util.writeUnsignedLeb128(value);
//    decoded = Util.readUnsignedLeb128(encoded);
//    assertEquals(value, decoded, "Decoded value should match original value for 0x7FFFFFFF");
//  }
//
//  @Test
//  public void testParseValidContent() {
//    Bytes byteData = Bytes.fromHexString("0x050102030405"); // Example with valid LEB128 encoding
//    List<Bytes> contents = Util.parseAcceptedContents(byteData);
//
//    assertEquals(1, contents.size());
//    assertEquals(Bytes.fromHexString("0x0102030405"), contents.get(0));
//  }
//
//  @Test
//  public void testParseZeroLengthContent() {
//    Bytes byteData = Bytes.fromHexString("0x00"); // Zero-length content
//    List<Bytes> contents = Util.parseAcceptedContents(byteData);
//
//    assertEquals(1, contents.size());
//    assertEquals(Bytes.fromHexString("0x00"), contents.get(0));
//  }
//
//  @Test
//  public void testParseMultipleContents() {
//    Bytes byteData =
//        Bytes.concatenate(
//            Util.writeUnsignedLeb128(DefaultContent.value1.size()),
//            DefaultContent.value1,
//            Util.writeUnsignedLeb128(DefaultContent.value2.size()),
//            DefaultContent.value2);
//
//    List<Bytes> contents = Util.parseAcceptedContents(byteData);
//
//    assertEquals(2, contents.size());
//    assertEquals(DefaultContent.value1, contents.get(0));
//    assertEquals(DefaultContent.value2, contents.get(1));
//  }
//
//  @Test
//  public void testParseInvalidLeb128() {
//    Bytes byteData = Bytes.fromHexString("0xFF"); // Invalid LEB128 encoding
//    assertThrows(Exception.class, () -> Util.parseAcceptedContents(byteData));
//  }
}
