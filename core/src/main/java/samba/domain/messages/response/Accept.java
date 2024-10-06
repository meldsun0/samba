package samba.domain.messages.response;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.*;
import samba.schema.ssz.containers.AcceptContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/***
 * Response message to Offer (0x06).
 */
public class Accept implements PortalWireMessage {

    private final int connectionId;
    private final Bytes contentKeys;

    public Accept(int connectionId, Bytes contentKeys) {

        //content_keys limit 64
        this.connectionId = connectionId;
        this.contentKeys = contentKeys;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ACCEPT;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public Bytes getContentKeys() {
        return contentKeys;
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(),
            new AcceptContainer(Bytes.ofUnsignedShort(connectionId), contentKeys).sszSerialize()
        );
    }
}
