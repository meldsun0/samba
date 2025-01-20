package samba.utp.network;

import samba.utp.data.UtpPacket;

import java.io.IOException;
import java.net.SocketAddress;

public interface TransportLayer {

  void sendPacket(UtpPacket packet) throws IOException;

  UtpPacket onPacketReceive() throws IOException;

  SocketAddress getRemoteAddress();

  void close();
}
