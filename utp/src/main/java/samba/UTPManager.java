package samba;

import org.apache.tuweni.bytes.Bytes;

import java.util.concurrent.CompletableFuture;

public interface UTPManager {

    CompletableFuture<Void> start();

    void stop();

    CompletableFuture<Bytes> receiveContent();

    CompletableFuture<Void> sendContent(Bytes content);

}
