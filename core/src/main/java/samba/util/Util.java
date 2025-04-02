package samba.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

public class Util {

  public static Bytes addUnsignedLeb128SizeToData(Bytes data) {
    checkArgument(data != null, "DATA must not be null");
    checkArgument(!data.isEmpty(), "Data MUST NOT be empty");

    if (data.toHexString().equals("0x00")) return Bytes.concatenate(Bytes.of(0));
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
      output = Bytes.concatenate(output, (Bytes.of(((value & 0x7f) | 0x80))));
      value = remaining;
      remaining >>>= 7;
    }

    return Bytes.concatenate(output, (Bytes.of(value & 0x7f)));
  }

  public static int readUnsignedLeb128(Bytes input) {
    int value = 0;
    int shift = 0;
    int index = 0;
    byte currentByte;

    while (true) {
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
}
