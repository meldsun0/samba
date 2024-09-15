package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import org.apache.tuweni.bytes.Bytes;

/***
 * Response message to Offer (0x06).
 */
public class Accept implements PortalWireMessage {

    private final UInt64 connectionId;
    private final Bytes content_keys;

    public Accept(UInt64 connectionId, Bytes contentKeys) {

        //content_keys limit 64
        this.connectionId = connectionId;
        this.content_keys = contentKeys;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ACCEPT;
    }

    public UInt64 getConnectionId() {
        return connectionId;
    }

    public Bytes getContentKeys() {
        return content_keys;
    }
}
