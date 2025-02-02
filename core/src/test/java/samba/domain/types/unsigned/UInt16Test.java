package samba.domain.types.unsigned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class UInt16Test {

  @Test
  public void testValueOf() {
    UInt16 value = UInt16.valueOf(0);
    assertEquals(0, value.getValue());
    value = UInt16.valueOf(0xFFFF);
    assertEquals(0xFFFF, value.getValue());

    IllegalArgumentException exception1 =
        assertThrows(IllegalArgumentException.class, () -> UInt16.valueOf(-1));
    assertEquals("Value out of range: " + -1, exception1.getMessage());

    IllegalArgumentException exception2 =
        assertThrows(IllegalArgumentException.class, () -> UInt16.valueOf(0x10000));
    assertEquals("Value out of range: " + 0x10000, exception2.getMessage());
  }

  @Test
  public void testToBytes() {
    UInt16 value = UInt16.valueOf(0);
    assertEquals(Bytes.of(0, 0), value.toBytes());
    value = UInt16.valueOf(0xFFFF);
    assertEquals(Bytes.of(0xFF, 0xFF), value.toBytes());
  }

  @Test
  public void testFromBytes() {
    UInt16 value = UInt16.fromBytes(Bytes.of(0, 0));
    assertEquals(0, value.getValue());
    value = UInt16.fromBytes(Bytes.of(0xFF, 0xFF));
    assertEquals(0xFFFF, value.getValue());

    IllegalArgumentException exception1 =
        assertThrows(IllegalArgumentException.class, () -> UInt16.fromBytes(Bytes.of(0)));
    assertEquals("UInt16 must be exactly 2 bytes", exception1.getMessage());

    IllegalArgumentException exception2 =
        assertThrows(IllegalArgumentException.class, () -> UInt16.fromBytes(Bytes.of(0, 0, 0)));
    assertEquals("UInt16 must be exactly 2 bytes", exception2.getMessage());
  }

  @Test
  public void testAdd() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0x68AC, value.add(other).getValue());
  }

  @Test
  public void testSubtract() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0xBBBC, value.subtract(other).getValue());
  }

  @Test
  public void testMultiply() {
    UInt16 value = UInt16.valueOf(0x000C);
    UInt16 other = UInt16.valueOf(0x0022);
    assertEquals(0x0198, value.multiply(other).getValue());
  }

  @Test
  public void testDivide() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0, value.divide(other).getValue());
  }

  @Test
  public void testRemainder() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0x1234, value.remainder(other).getValue());
  }

  @Test
  public void testAnd() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0x1230, value.and(other).getValue());
  }

  @Test
  public void testOr() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0x567C, value.or(other).getValue());
  }

  @Test
  public void testXor() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(0x444C, value.xor(other).getValue());
  }

  @Test
  public void testNot() {
    UInt16 value = UInt16.valueOf(0x1234);
    assertEquals(0xEDCB, value.not().getValue());
  }

  @Test
  public void testShiftLeft() {
    UInt16 value = UInt16.valueOf(0x1234);
    assertEquals(0x2468, value.shiftLeft(1).getValue());
    assertEquals(0x48D0, value.shiftLeft(2).getValue());
    assertEquals(0x91A0, value.shiftLeft(3).getValue());
    assertEquals(0x2340, value.shiftLeft(4).getValue());
    assertEquals(0x4680, value.shiftLeft(5).getValue());
    assertEquals(0x8D00, value.shiftLeft(6).getValue());
    assertEquals(0x1A00, value.shiftLeft(7).getValue());
    assertEquals(0x3400, value.shiftLeft(8).getValue());
    assertEquals(0x6800, value.shiftLeft(9).getValue());
    assertEquals(0xD000, value.shiftLeft(10).getValue());
    assertEquals(0xA000, value.shiftLeft(11).getValue());
    assertEquals(0x4000, value.shiftLeft(12).getValue());
    assertEquals(0x8000, value.shiftLeft(13).getValue());
    assertEquals(0x0, value.shiftLeft(14).getValue());
    assertEquals(0x0, value.shiftLeft(15).getValue());
  }

  @Test
  public void testShiftRight() {
    UInt16 value = UInt16.valueOf(0x1234);
    assertEquals(0x091A, value.shiftRight(1).getValue());
    assertEquals(0x048D, value.shiftRight(2).getValue());
    assertEquals(0x0246, value.shiftRight(3).getValue());
    assertEquals(0x0123, value.shiftRight(4).getValue());
    assertEquals(0x0091, value.shiftRight(5).getValue());
    assertEquals(0x0048, value.shiftRight(6).getValue());
    assertEquals(0x0024, value.shiftRight(7).getValue());
    assertEquals(0x0012, value.shiftRight(8).getValue());
    assertEquals(0x0009, value.shiftRight(9).getValue());
    assertEquals(0x0004, value.shiftRight(10).getValue());
    assertEquals(0x0002, value.shiftRight(11).getValue());
    assertEquals(0x0001, value.shiftRight(12).getValue());
    assertEquals(0x0000, value.shiftRight(13).getValue());
    assertEquals(0x0000, value.shiftRight(14).getValue());
    assertEquals(0x0000, value.shiftRight(15).getValue());
  }

  @Test
  public void testCompareTo() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(-1, value.compareTo(other));
    assertEquals(1, other.compareTo(value));
  }

  @Test
  public void testEquals() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(false, value.equals(other));
    assertEquals(false, other.equals(value));
  }

  @Test
  public void testHashCode() {
    UInt16 value = UInt16.valueOf(0x1234);
    UInt16 other = UInt16.valueOf(0x5678);
    assertEquals(4660, value.hashCode());
    assertEquals(22136, other.hashCode());
  }
}
