package samba.services.utp;

import org.apache.tuweni.bytes.Bytes;

import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface UTP {
    public SafeFuture<?> connect(int connectionId);

    public SafeFuture<?> sendContent(int connectionId, Bytes content);

    public SafeFuture<Bytes> getContent(int connectionId);
}