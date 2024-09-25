package samba.domain.messages.requests;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer implements PortalWireMessage {

    private final List<Bytes> content_keys;

    public Offer(List<Bytes> contentKeys) {
        checkArgument(contentKeys != null && contentKeys.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "contentKeys cannot be null or exceed maximum payload size");
        checkArgument(contentKeys.stream().allMatch(key -> key.size() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more content keys exceed maximum payload size");
        this.content_keys = contentKeys;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OFFER;
    }

    public List<Bytes> getContentKeys() {
        return content_keys;
    }

    @Override
    public Bytes serialize() {
        Bytes contentKeysSerialized = SSZ.encodeBytesList(content_keys);
        return Bytes.concatenate(
                SSZ.encodeUInt8(getMessageType().ordinal()),
                contentKeysSerialized);
    }

    @Override
    public Offer getMessage() {
        return this;
    }
}
