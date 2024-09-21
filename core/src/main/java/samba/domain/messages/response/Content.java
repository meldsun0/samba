package samba.domain.messages.response;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.MessageType;

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

    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }
}
