package samba.utp.network;

import static samba.utp.data.UtpPacketUtils.MAX_UDP_HEADER_LENGTH;
import static samba.utp.data.UtpPacketUtils.MAX_UTP_PACKET_LENGTH;

import samba.utp.data.UtpPacket;
import samba.utp.message.UTPWireMessageDecoder;

import java.io.IOException;
import java.net.*;

public class UDPTransportLayer implements TransportLayer {

  protected DatagramSocket socket;
  private final Object sendLock = new Object();
  private InetSocketAddress serverSocketAddress;

  public UDPTransportLayer(String serverAddress, int serverPort) {
    try {
      this.socket = new DatagramSocket();
      this.serverSocketAddress = new InetSocketAddress(serverAddress, serverPort);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  public UDPTransportLayer(InetSocketAddress address) {
    try {
      this.socket = new DatagramSocket(address);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendPacket(UtpPacket UTPPacket) throws IOException {
    synchronized (sendLock) {
      DatagramPacket UDPPacket = UtpPacket.createDatagramPacket(UTPPacket);
      UDPPacket.setAddress(serverSocketAddress.getAddress());
      UDPPacket.setPort(serverSocketAddress.getPort());
      this.socket.send(UDPPacket);
    }
  }

  @Override
  public UtpPacket onPacketReceive() throws IOException {
    byte[] buffer = new byte[MAX_UDP_HEADER_LENGTH + MAX_UTP_PACKET_LENGTH];
    DatagramPacket dgpkt = new DatagramPacket(buffer, buffer.length);
    this.socket.receive(dgpkt);
    return UTPWireMessageDecoder.decode(dgpkt);
  }

  @Override
  public SocketAddress getRemoteAddress() {
    return this.serverSocketAddress;
  }

  @Override
  public void close() {
    this.socket.close();
  }
}
