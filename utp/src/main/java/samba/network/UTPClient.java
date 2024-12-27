package samba.network;

import org.apache.tuweni.bytes.Bytes;

import java.net.InetSocketAddress;

/** UTP client sends outgoing messages */
public interface UTPClient {

  void stop();

  void send(Bytes data, InetSocketAddress destination);
}
