package examples;

import samba.utp.UTPClient;
import samba.utp.network.TransportLayer;
import samba.utp.network.udp.UDPAddress;
import samba.utp.network.udp.UDPTransportLayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GetContent {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    UDPAddress udpaddress = new UDPAddress("localhost", 13345);
    UDPTransportLayer udpTransportLayer = new UDPTransportLayer();

    UTPClient chanel = new UTPClient(udpTransportLayer);

    startListeningIncomingPackets(udpTransportLayer, chanel);

    chanel
        .connect(333, udpaddress)
        .thenCompose(v -> chanel.read())
        .thenApply(
            (data) -> {
              System.out.println(StandardCharsets.UTF_8.decode(data));
              return CompletableFuture.completedFuture(data);
            })
        .get();
  }

  public static void startListeningIncomingPackets(
      TransportLayer transportLayer, UTPClient utpClient) {
    CompletableFuture.runAsync(
        () -> {
          while (true) {
            if (utpClient.isAlive()) {
              try {
                utpClient.receivePacket(transportLayer.onPacketReceive());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        });
  }
}
