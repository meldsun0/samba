package samba.network;

import samba.pipeline.Envelope;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.Publisher;

/** UTP server which listens to incoming messages according to setup */
public interface UTPServer {

  CompletableFuture<?> start();

  void stop();

  InetSocketAddress getListenAddress();

  /** Raw incoming packets stream */
  Publisher<Envelope> getIncomingPackets();
}
