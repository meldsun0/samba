package samba.utp.network.udp;

import samba.utp.network.TransportAddress;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPAddress implements TransportAddress {

  private InetSocketAddress address;

  public UDPAddress(String remoteAddress, int port) {
    this.address = new InetSocketAddress(remoteAddress, port);
  }

  @Override
  public InetAddress getAddress() {
    return this.address.getAddress();
  }

  public int getPort() {
    return this.address.getPort();
  }
}
