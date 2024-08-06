package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

/**
 * Response message to Find Content (0x04).
 */
public class Content
{
    private final UInt64 connectionId;
    private final byte[] payload;
    private final Byte[][] enrs;
    public Content(UInt64 connectionId, byte[] payload, Byte[][] enrs) {
        this.connectionId = connectionId;
        this.payload = payload;
        this.enrs = enrs;
    }
}
