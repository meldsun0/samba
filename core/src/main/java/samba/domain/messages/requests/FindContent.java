package samba.domain.messages.requests;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.messages.ssz.containers.FindContentContainer;
import samba.schema.messages.ssz.containers.FindNodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to get the content with content_key.
 */
public class FindContent implements PortalWireMessage {

    private final Bytes contentKey;

    public FindContent(Bytes contentKey) {
        checkArgument(contentKey != null && contentKey.size() <= MAX_CUSTOM_PAYLOAD_BYTES, "contentKey cannot be null or exceed maximum payload size");
        this.contentKey = contentKey;
    }

    public static FindContent fromSSZBytes(Bytes sszbytes){
        Bytes container = sszbytes.slice(1);
        FindContentContainer findContentContainer = FindContentContainer.decodePacket(container);
        Bytes contentKey = findContentContainer.getContentKey();

        if (contentKey.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("FINDCONTENT: Content key size exceeds limit");
        }
        return new FindContent(contentKey);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_CONTENT;
    }

    public Bytes getContentKey() {
        return contentKey;
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new FindNodesContainer(contentKey).sszSerialize());
    }

    @Override
    public FindContent getMessage() {
        return this;
    }
}
