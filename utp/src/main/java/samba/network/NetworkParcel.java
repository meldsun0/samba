package samba.network;

import samba.packet.RawPacket;

import java.net.InetSocketAddress;


public interface NetworkParcel {
  RawPacket getPacket();
  InetSocketAddress getDestination();
}
