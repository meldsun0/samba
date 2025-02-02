package samba.domain.types.unsigned;

import org.apache.tuweni.bytes.Bytes;

public final class UInt16 implements Comparable<UInt16> {

  public static final UInt16 ZERO = new UInt16(0);
  public static final UInt16 MAX_VALUE = new UInt16(0xFFFF);

  private final int value;

  private UInt16(int value) {
    this.value = value;
  }

  public static UInt16 valueOf(int value) {
    if (value < 0 || value > 0xFFFF) {
      throw new IllegalArgumentException("Value out of range: " + value);
    }
    return new UInt16(value);
  }

  public int getValue() {
    return value;
  }

  public Bytes toBytes() {
    return Bytes.of((byte) (value >> 8), (byte) value);
  }

  public static UInt16 fromBytes(Bytes bytes) {
    if (bytes.size() != 2) {
      throw new IllegalArgumentException("UInt16 must be exactly 2 bytes");
    }
    int value = ((bytes.get(0) & 0xFF) << 8) | (bytes.get(1) & 0xFF);
    return new UInt16(value);
  }

  public UInt16 add(UInt16 other) {
    return new UInt16((value + other.value) & 0xFFFF);
  }

  public UInt16 subtract(UInt16 other) {
    return new UInt16((value - other.value) & 0xFFFF);
  }

  public UInt16 multiply(UInt16 other) {
    return new UInt16((value * other.value) & 0xFFFF);
  }

  public UInt16 divide(UInt16 other) {
    return new UInt16(value / other.value);
  }

  public UInt16 remainder(UInt16 other) {
    return new UInt16(value % other.value);
  }

  public UInt16 and(UInt16 other) {
    return new UInt16(value & other.value);
  }

  public UInt16 or(UInt16 other) {
    return new UInt16(value | other.value);
  }

  public UInt16 xor(UInt16 other) {
    return new UInt16(value ^ other.value);
  }

  public UInt16 not() {
    return new UInt16(~value & 0xFFFF);
  }

  public UInt16 shiftLeft(int n) {
    return new UInt16((value << n) & 0xFFFF);
  }

  public UInt16 shiftRight(int n) {
    return new UInt16(value >>> n);
  }

  @Override
  public int compareTo(UInt16 other) {
    return Integer.compare(this.value, other.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UInt16 uInt16) {
      return this.value == uInt16.value;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
