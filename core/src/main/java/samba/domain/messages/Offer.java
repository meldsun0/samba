package samba.domain.messages;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer implements PortalWireMessage {

    private static final int MAX_KEYS = 64;
    private final Byte[][] content_keys;

    public Offer(Byte[][] contentKeys) {
        checkArgument(contentKeys != null && contentKeys.length <= MAX_CUSTOM_PAYLOAD_SIZE, "contentKeys cannot be null or exceed maximum payload size");
        checkArgument(Arrays.stream(contentKeys).allMatch(key -> key.length <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more content keys exceed maximum payload size");
        this.content_keys = contentKeys;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OFFER;
    }

    public Byte[][] getContentKeys() {
        return content_keys;
    }
}
