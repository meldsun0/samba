package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to get the content with content_key.
 * In case the recipient does not have the data, a list of ENR records of nodes that
 * are closest to the requested content.
 */
public class FindContent implements PortalWireMessage {

    private final Byte[] contentKey;

    public FindContent(Byte[] contentKey) {
        checkArgument(contentKey != null && contentKey.length <= MAX_CUSTOM_PAYLOAD_SIZE, "contentKey cannot be null or exceed maximum payload size");
        
        this.contentKey = contentKey;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_CONTENT;
    }

    public Byte[] getContentKey() {
        return contentKey;
    }

}
