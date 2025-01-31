package samba.utp.data.util;

import org.apache.tuweni.bytes.Bytes;

public class Utils {

  public static final long MAX_SEQUENCE_NR = 65535;
  private static final long MAX_UINT32 = 0xFFFFFFFFL;

  private static final int MAX_UINT16 = 0xFFFF; // 65535

  public static int randomSeqNumber() {
    return Bytes.ofUnsignedInt((int) (Math.random() * (MAX_SEQUENCE_NR - 1)) + 1).toInt();
  }

  public static int incrementSeqNumber(int currentSequenceNumber) {
    int seqNumber = currentSequenceNumber + 1;
    return Bytes.ofUnsignedInt(seqNumber > MAX_SEQUENCE_NR ? 1 : seqNumber).toInt();
  }

  public static boolean isConnectionValid(int connectionId) {
    return isValidUInt16(connectionId);
  }

  private static boolean isValidUInt16(int value) {
    if (value >= 0 && value <= MAX_UINT16) {
      Bytes bytes = Bytes.ofUnsignedShort(value); // short has 2 bytes.
      return bytes.size() == 2;
    }
    return false;
  }
}
