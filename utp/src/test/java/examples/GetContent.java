package examples;

import samba.utp.UTPClient;
import samba.utp.network.udp.UDPAddress;
import samba.utp.network.udp.UDPTransportLayer;

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
              System.out.println(data);
              return CompletableFuture.completedFuture(data);
            })
        .get();
  }

  public static void startListeningIncomingPackets(
      UDPTransportLayer transportLayer, UTPClient utpClient) {
    CompletableFuture.runAsync(
        () -> {
          while (true) {
            if (utpClient.isAlive()) {
              //              try {
              ////                utpClient.receivePacket(transportLayer.onPacketReceive(), new
              // TransportAddress() {
              ////                    @Override
              ////                    public Object getAddress() {
              ////                        return null;
              ////                    }
              ////                });
              ////              } catch (IOException e) {
              ////                throw new RuntimeException(e);
              //              }
            }
          }
        });
  }
}
