package samba.utp.handlers;

import samba.utp.Session;

import java.net.DatagramPacket;

public interface UTPMessageHandler<Message> {

  void handle(Session utpSession, DatagramPacket udpPacket);
}
