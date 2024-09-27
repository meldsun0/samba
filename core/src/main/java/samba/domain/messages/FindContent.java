package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.schema.ssz.containers.FindNodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to get the content with content_key.
 * In case the recipient does not have the data, a list of ENR records of nodes that
 * are closest to the requested content.
 */
public class FindContent implements PortalWireMessage {

    private final Bytes contentKey;

    public FindContent(Bytes contentKey) {
        checkArgument(contentKey != null && contentKey.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "contentKey cannot be null or exceed maximum payload size");
        
        this.contentKey = contentKey;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_CONTENT;
    }

    public Bytes getContentKey() {
        return contentKey;
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new FindNodesContainer(contentKey).sszSerialize());
    }
}
