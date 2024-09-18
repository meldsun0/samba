package samba.domain.messages.requests;

import org.apache.tuweni.bytes.Bytes;
import samba.domain.messages.MessageType;
import samba.domain.messages.HistoryProtocolRequestMessage;

/**
 * Request message to get the content with content_key.
 * In case the recipient does not have the data, a list of ENR records of nodes that
 * are closest to the requested content.
 */
public class FindContent implements HistoryProtocolRequestMessage {

    private final Byte[] contentKey;

    public FindContent(Byte[] contentKey) {
        this.contentKey = contentKey;
    }

    public MessageType getMessageType() {
        return MessageType.FIND_CONTENT;
    }

    @Override
    public Bytes getMessageInBytes() {
        null;
    }

    @Override
    public MessageType getType() {
        return null;
    }

    @Override
    public Bytes getSSZMessageInBytes() {
        return null;
    }
}
