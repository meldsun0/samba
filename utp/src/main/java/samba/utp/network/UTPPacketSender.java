package samba.utp.network;

import samba.utp.data.UtpPacket;

@FunctionalInterface
public interface UTPPacketSender {
    void send(UtpPacket packet, TransportAddress remoteAddress);
}