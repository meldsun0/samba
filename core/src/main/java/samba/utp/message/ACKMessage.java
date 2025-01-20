package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.STATE;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUint;
import static samba.utp.data.bytes.UnsignedTypesUtil.longToUshort;

import samba.utp.data.UtpPacket;

public class ACKMessage {

  public static UtpPacket build(
      int timeDifference,
      long advertisedWindow,
      int timestamp,
      long connectionIdSending,
      int ackNumber,
      byte firstExtension) {
    return UtpPacket.builder()
        .typeVersion(STATE)
        .firstExtension(firstExtension)
        .connectionId(longToUshort(connectionIdSending))
        .timestamp(timestamp)
        .timestampDifference(timeDifference)
        .windowSize(longToUint(advertisedWindow))
        .ackNumber(longToUshort(ackNumber))
        .build();
  }
}
