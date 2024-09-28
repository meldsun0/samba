package samba.domain.messages;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.schema.ssz.containers.OfferContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer implements PortalWireMessage {

    private final List<Bytes> contentKeys;

    public Offer(List<Bytes> contentKeys) {
        checkArgument(contentKeys != null && contentKeys.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "contentKeys cannot be null or exceed maximum payload size");
        checkArgument(contentKeys.stream().allMatch(key -> key.size() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more content keys exceed maximum payload size");
        this.contentKeys = contentKeys;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OFFER;
    }

    public List<Bytes> getContentKeys() {
        return contentKeys;
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                new OfferContainer(contentKeys).sszSerialize());
    }
}
