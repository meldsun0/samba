package samba.utp.network.udp;

import static samba.utp.data.UtpPacketUtils.MAX_UDP_HEADER_LENGTH;
import static samba.utp.data.UtpPacketUtils.MAX_UTP_PACKET_LENGTH;

import samba.utp.data.UtpPacket;
import samba.utp.message.UTPWireMessageDecoder;
import samba.utp.network.TransportLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPTransportLayer implements TransportLayer<UDPAddress> {

  protected DatagramSocket socket;
  private final Object sendLock = new Object();

  public UDPTransportLayer() {
    try {
      this.socket = new DatagramSocket();
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendPacket(UtpPacket packet, UDPAddress remoteAddress) throws IOException {
    synchronized (sendLock) {
      DatagramPacket UDPPacket = UtpPacket.createDatagramPacket(packet);
      UDPPacket.setAddress(remoteAddress.getAddress());
      UDPPacket.setPort(remoteAddress.getPort());
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
  public void close() {
    this.socket.close();
  }
}
