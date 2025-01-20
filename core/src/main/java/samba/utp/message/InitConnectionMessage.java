package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.SYN;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUbyte;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;

import samba.utp.data.UtpPacket;

public class InitConnectionMessage {

  public static UtpPacket build(int timestamp, long connectionId) {
    return UtpPacket.builder()
        .typeVersion(SYN)
        .sequenceNumber(longToUbyte(1))
        .payload(new byte[] {1, 2, 3, 4, 5, 6})
        .connectionId(longToUshort(connectionId))
        .timestamp(timestamp)
        .build();
  }
}
