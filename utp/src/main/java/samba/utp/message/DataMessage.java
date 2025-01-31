package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.DATA;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;

import samba.utp.data.UtpPacket;

public class DataMessage {

  public static UtpPacket build(
      int timestamp, long connectionIdSending, int ackNumber, int sequenceNumber) {
    return UtpPacket.builder()
        .typeVersion(DATA)
        .connectionId(longToUshort(connectionIdSending))
        .timestamp(timestamp)
        .ackNumber(longToUshort(ackNumber))
        .sequenceNumber(longToUshort(sequenceNumber))
        .build();
  }
}
