package samba.domain.messages.response;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.*;
import samba.domain.messages.requests.Ping;
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

    public static Accept fromSSZBytes(Bytes sszbytes) {
        Bytes container = sszbytes.slice(1);
        AcceptContainer acceptContainer = AcceptContainer.decodePacket(container);
        int connectionId = acceptContainer.getConnectionId();
        Bytes contentKeys = acceptContainer.getContentKeysBitList();

        if (contentKeys.size() > PortalWireMessage.MAX_KEYS / 8) {
            throw new IllegalArgumentException("ACCEPT: Number of content keys exceeds limit");
        }
        return new Accept(connectionId, contentKeys);
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
    public Bytes getSszBytes() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                new AcceptContainer(Bytes.ofUnsignedShort(connectionId), contentKeys).sszSerialize()
        );
    }

    @Override
    public Accept getMessage() {
        return this;
    }
}
