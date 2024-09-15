package samba.domain.messages;

import java.util.Arrays;
import java.util.List;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to Find Content (0x04).
 */
public class Content implements PortalWireMessage {

    private final UInt64 connectionId;
    private final Bytes content;
    private final List<Bytes> enrs;
    private final int payloadType;

    public Content(UInt64 connectionId) {
        this.payloadType = 0;
        this.connectionId = connectionId;
        this.content = null;
        this.enrs = null;

    }

    public Content(Bytes content) {
        checkArgument(content.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Content size exceeds limit");
        this.payloadType = 1;
        this.content = content;
        this.connectionId = null;
        this.enrs = null;
    }

    public Content(List<Bytes> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.size() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        this.payloadType = 2;
        this.enrs = enrs;
        this.connectionId = null;
        this.content = null;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }

    public UInt64 getConnectionId() {
        return connectionId;
    }

    public Bytes getContent() {
        return content;
    }

    public List<Bytes> getEnrList() {
        return enrs;
    }
}
