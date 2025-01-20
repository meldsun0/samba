package samba.utp.examples;

import samba.utp.UTPClient;
import samba.utp.network.TransportLayer;
import samba.utp.network.UDPTransportLayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GetContent {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    UDPTransportLayer udpTransportLayer = new UDPTransportLayer("localhost", 13345);

    UTPClient chanel = new UTPClient(udpTransportLayer);
    startListeningIncomingPackets(udpTransportLayer, chanel);
    ByteBuffer buffer = ByteBuffer.allocate(10);

    chanel
        .connect(333)
        .thenCompose(v -> chanel.read(buffer))
        .thenRun(
            () -> {
              saveAnswerOnFile(buffer, "content");
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

  public static void saveAnswerOnFile(ByteBuffer byteBuffer, String name) {
    if (byteBuffer != null) {
      try {
        byteBuffer.flip();
        File outFile = new File("testData/" + name + ".data");
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        FileChannel fchannel = fileOutputStream.getChannel();
        while (byteBuffer.hasRemaining()) {
          fchannel.write(byteBuffer);
        }
        fchannel.close();
        fileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
