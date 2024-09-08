package samba.domain.messages;

import java.util.Arrays;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to Find Content (0x04).
 */
public class Content implements PortalWireMessage {

    private final static int MAX_ENRS = 32;
    private final UInt64 connectionId;
    private final Byte[] content;
    private final Byte[][] enrs;
    public Content(UInt64 connectionId, Byte[] content, Byte[][] enrs) {
        checkArgument(connectionId != null && UInt64.ZERO.compareTo(connectionId) < 0, "connectionId cannot be null or negative");
        checkArgument(content.length <= MAX_CUSTOM_PAYLOAD_SIZE, "Content size exceeds limit");
        checkArgument(enrs.length <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(Arrays.stream(enrs).allMatch(enr -> enr.length <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        this.connectionId = connectionId;
        this.content = content;
        this.enrs = enrs;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }

    public UInt64 getConnectionId() {
        return connectionId;
    }

    public Byte[] getContent() {
        return content;
    }

    public Byte[][] getEnrArray() {
        return enrs;
    }
}
