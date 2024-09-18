package samba.domain.messages.requests;

import org.apache.tuweni.bytes.Bytes;
import samba.domain.messages.MessageType;
import samba.domain.messages.HistoryProtocolRequestMessage;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer implements HistoryProtocolRequestMessage {

    private final Byte[][] content_keys;

    public Offer(Byte[][] contentKeys) {
        content_keys = contentKeys;
    }

    public MessageType getMessageType() {
        return MessageType.OFFER;
    }

    @Override
    public Bytes getMessageInBytes() {
        return null;
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
