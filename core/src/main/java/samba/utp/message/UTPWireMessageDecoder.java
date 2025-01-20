package samba.utp.message;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import samba.utp.data.UtpPacket;

import java.net.DatagramPacket;

public class UTPWireMessageDecoder {

  public static final int DEF_HEADER_LENGTH = 20;

  public static UtpPacket decode(DatagramPacket udpPacket) {
    checkNotNull(udpPacket, "UDPPacket could not be null when decoding a UTP Wire Message");
    checkNotNull(udpPacket.getData(), "UDPPacket should have data");
    checkArgument(
        udpPacket.getData().length >= DEF_HEADER_LENGTH,
        "UDPPacket data should have more than 1 bytes when decoding a UTP Message");
    checkNotNull(getMessageType(udpPacket), "Invalid message type");

    return UtpPacket.decode(udpPacket);
  }

  private static MessageType getMessageType(DatagramPacket udpPacket) {
    byte packetType = udpPacket.getData()[0];
    return MessageType.fromByte(packetType);
  }
}
