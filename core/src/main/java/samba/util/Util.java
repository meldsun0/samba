package samba.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

public class Util {

  public static Bytes addUnsignedLeb128SizeToData(Bytes data) {
    checkArgument(data != null, "DATA must not be null");
    // checkArgument(!data.isEmpty(), "Data MUST NOT be empty");
    // TODO FIX
    if (data.equals(Bytes.of(0)) || data.equals(Bytes.EMPTY) || data.isEmpty()) {
      return Bytes.of(0);
    }
    return Bytes.concatenate(Util.writeUnsignedLeb128(data.size()), data);
  }

  /**
   * Given an int value return unsignedLeb128 byte
   *
   * @param value
   * @return
   */
  public static Bytes writeUnsignedLeb128(int value) {
    Bytes output = Bytes.EMPTY;
    int remaining = value >>> 7;
    while (remaining != 0) {
      output = Bytes.concatenate(output, Bytes.of((byte) ((value & 0x7f) | 0x80)));
      value = remaining;
      remaining >>>= 7;
    }
    return Bytes.concatenate(output, Bytes.of((byte) (value & 0x7f)));
  }

  public static int readUnsignedLeb128(Bytes input) {
    int value = 0;
    int shift = 0;
    int index = 0;
    byte currentByte;

    while (true) {
      if (index >= input.size()) {
        throw new IllegalArgumentException("Invalid LEB128 encoding: input truncated");
      }
      currentByte = input.get(index);
      index++;
      value |= (currentByte & 0x7f) << shift;
      if ((currentByte & 0x80) == 0) {
        break;
      }
      shift += 7;
    }

    return value;
  }

  public static int getLeb128Length(int value) {
    int length = 1;
    while ((value >>> (7 * length)) != 0) {
      length++;
    }
    return length;
  }

  public static List<Bytes> parseAcceptedContents(Bytes byteData) {
    List<Bytes> contents = new ArrayList<>();
    int index = 0;
    while (index < byteData.size()) {
      int sizeOfContent = Util.readUnsignedLeb128(byteData.slice(index));
      if (sizeOfContent == 0) {
        index = index + 6;
        contents.add(Bytes.fromHexString("0x00"));
        continue;
      }
      index += Util.getLeb128Length(sizeOfContent);
      Bytes content = byteData.slice(index, sizeOfContent);
      contents.add(content);
      index += sizeOfContent;
    }
    return contents;
  }

  public static Bytes parseAcceptedContent(Bytes byteData) {
    int sizeOfContent = Util.readUnsignedLeb128(byteData);
    if (sizeOfContent == 0) {
      return Bytes.fromHexString("0x00");
    }
    return byteData.slice(Util.getLeb128Length(sizeOfContent), sizeOfContent);
  }
}
