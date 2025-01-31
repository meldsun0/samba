package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.FIN;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;

import samba.utp.data.UtpPacket;

public class FinMessage {

  public static UtpPacket build(
      int timestamp, long connectionIdSending, int ackNumber, int sequenceNumber) {
    // TODO not use but do not forget to   this.currentSequenceNumber =
    // Utils.incrementSeqNumber(this.currentSequenceNumber);
    return UtpPacket.builder()
        .typeVersion(FIN)
        .connectionId(longToUshort(connectionIdSending))
        .timestamp(timestamp)
        .ackNumber(longToUshort(ackNumber))
        .sequenceNumber(longToUshort(sequenceNumber))
        .build();
  }
}
