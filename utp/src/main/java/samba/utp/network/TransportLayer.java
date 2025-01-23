package samba.utp.network;

import samba.utp.data.UtpPacket;

import java.io.IOException;

public interface TransportLayer<T extends TransportAddress> {

  void sendPacket(UtpPacket packet, T remoteAddress) throws IOException;

  UtpPacket onPacketReceive() throws IOException;

  void close();
}
