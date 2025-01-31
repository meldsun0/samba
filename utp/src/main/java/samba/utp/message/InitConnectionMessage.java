package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.SYN;
import static samba.utp.data.bytes.UnsignedTypesUtil.*;

import samba.utp.data.UtpPacket;

public class InitConnectionMessage {

  public static UtpPacket build(int timestamp, long connectionId, long advertisedWindow) {
    return UtpPacket.builder()
        .typeVersion(SYN)
        .sequenceNumber(longToUbyte(1))
        .timestampDifference(0)
        .windowSize(longToUint(advertisedWindow))
        .connectionId(longToUshort(connectionId))
        .timestamp(timestamp)
        .build();
  }
}
