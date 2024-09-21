package samba.domain.messages.response;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.MessageType;

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
