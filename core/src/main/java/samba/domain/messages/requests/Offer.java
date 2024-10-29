package samba.domain.messages.requests;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.ssz.containers.OfferContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer implements PortalWireMessage {

    private final List<Bytes> contentKeys;

    public Offer(List<Bytes> contentKeys) {
        checkArgument(contentKeys != null && contentKeys.size() <= MAX_CUSTOM_PAYLOAD_BYTES, "contentKeys cannot be null or exceed maximum payload size");
        checkArgument(contentKeys.stream().allMatch(key -> key.size() <= MAX_CUSTOM_PAYLOAD_BYTES), "One or more content keys exceed maximum payload size");
        this.contentKeys = contentKeys;
    }

    public static Offer fromSSZBytes(Bytes sszbytes) {
        Bytes container = sszbytes.slice(1);
        OfferContainer offerContainer = OfferContainer.decodePacket(container);
        List<Bytes> contentKeys = offerContainer.getContentKeys();

        if (contentKeys.size() > PortalWireMessage.MAX_KEYS) {
            throw new IllegalArgumentException("OFFER: Number of content keys exceeds limit");
        }
        for (Bytes key : contentKeys) {
            if (key.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
                throw new IllegalArgumentException("OFFER: One or more content keys exceed maximum payload size");
            }
        }
        return new Offer(contentKeys);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OFFER;
    }

    public List<Bytes> getContentKeys() {
        return contentKeys;
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                new OfferContainer(contentKeys).sszSerialize());
    }

    @Override
    public Offer getMessage() {
        return this;
    }
}
