package samba;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import samba.message.UTPMessage;

import java.util.concurrent.CompletableFuture;

public class UTPManagerImpl implements UTPManager {

    private static final Logger LOG = LogManager.getLogger();


    @Override
    public CompletableFuture<Void> start() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public CompletableFuture<Bytes> receiveContent() {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendContent(Bytes content) {
        return null;
    }
}
