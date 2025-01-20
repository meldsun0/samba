package samba.utp.examples;

import samba.utp.UTPClient;
import samba.utp.network.TransportLayer;
import samba.utp.network.UDPTransportLayer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Offer {

  public static void main(String args[])
      throws ExecutionException, InterruptedException, IOException {
    UDPTransportLayer udpTransportLayer = new UDPTransportLayer("localhost", 13345);

    UTPClient chanel = new UTPClient(udpTransportLayer);
    startListeningIncomingPackets(udpTransportLayer, chanel);

    chanel
        .connect(333)
        .thenCompose(
            v -> {
              try {
                return chanel.write(getFileToSend());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
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

  public static ByteBuffer getFileToSend() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(9);
    RandomAccessFile file = new RandomAccessFile("testData/sc S01E01.avi", "rw");
    FileChannel fileChannel = file.getChannel();
    int bytesRead;
    System.out.println("start reading from file");
    do {
      bytesRead = fileChannel.read(buffer);
    } while (bytesRead != -1);
    System.out.println("file read" + buffer.array().length);
    return buffer;
  }
}
