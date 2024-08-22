package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.BitSet;

/***
 * Response message to Offer (0x06).
 */
public class Accept {
    private final UInt64 connectionId;
    private final Byte[] content_keys;

    public Accept(UInt64 connectionId, Byte[] contentKeys) {
        //content_keys limit 64
        this.connectionId = connectionId;
        content_keys = contentKeys;
    }

    public MessageType getMessageType() {
        return MessageType.ACCEPT;
    }
}
