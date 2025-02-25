package samba.utp.message;

import static samba.utp.data.UtpPacketUtils.*;
import static samba.utp.data.bytes.UnsignedTypesUtil.*;

import samba.utp.data.UtpPacket;

public class MessageUtil {

  public static UtpPacket buildACKMessage(
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

  public static UtpPacket buildFINMessage(
      int timestamp, long connectionIdSending, int ackNumber, int sequenceNumber) {
    return UtpPacket.builder()
        .typeVersion(FIN)
        .connectionId(longToUshort(connectionIdSending))
        .timestamp(timestamp)
        .ackNumber(longToUshort(ackNumber))
        .sequenceNumber(longToUshort(sequenceNumber))
        .build();
  }

  public static UtpPacket buildDataMessage(
      int timestamp, long connectionIdSending, int ackNumber, int sequenceNumber) {
    return UtpPacket.builder()
        .typeVersion(DATA)
        .connectionId(longToUshort(connectionIdSending))
        .timestamp(timestamp)
        .ackNumber(longToUshort(ackNumber))
        .sequenceNumber(longToUshort(sequenceNumber))
        .build();
  }

  public static UtpPacket buildSYNMessage(
      int timestamp, long connectionId, long advertisedWindow, long seqNumber) {
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
