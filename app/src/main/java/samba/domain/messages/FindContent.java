package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

/**
 * Request message to get the content with content_key.
 * In case the recipient does not have the data, a list of ENR records of nodes that
 * are closest to the requested content.
 */
public class FindContent {

    private final Byte[] contentKey;

    public FindContent(Byte[] contentKey) {
        this.contentKey = contentKey;
    }

    public MessageType getMessageType() {
        return MessageType.FIND_CONTENT;
    }

}
