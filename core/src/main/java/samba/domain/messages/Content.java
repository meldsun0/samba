package samba.domain.messages;

import java.util.List;

import org.apache.tuweni.units.bigints.UInt64;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to Find Content (0x04).
 */
public class Content implements PortalWireMessage {

    private final int connectionId;
    private final Bytes content;
    private final List<Bytes> enrs;
    private final int payloadType;

    public Content(int connectionId) {
        this.payloadType = 0;
        this.connectionId = connectionId;
        this.content = null;
        this.enrs = null;

    }

    public Content(Bytes content) {
        checkArgument(content.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Content size exceeds limit");
        this.payloadType = 1;
        this.content = content;
        this.connectionId = 0;
        this.enrs = null;
    }

    public Content(List<Bytes> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.size() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        this.payloadType = 2;
        this.enrs = enrs;
        this.connectionId = 0;
        this.content = null;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }

    public int getPayloadType() {
        return payloadType;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public Bytes getContent() {
        return content;
    }

    public List<Bytes> getEnrList() {
        return enrs;
    }

    @Override
    public Bytes serialize() {
        Bytes payloadTypeSerialized = SSZ.encodeInt8(payloadType);
        switch(payloadType) {
            case 0 -> {
                Bytes connectionIdSerialized = SSZ.encodeBytes(Bytes.ofUnsignedShort(connectionId));
                return Bytes.concatenate(
                        SSZ.encodeUInt8(getMessageType().ordinal()),
                        payloadTypeSerialized,
                        connectionIdSerialized);
            }
            case 1 -> {
                Bytes contentSerialized = SSZ.encodeBytes(content);
                return Bytes.concatenate(
                        SSZ.encodeUInt8(getMessageType().ordinal()),
                        payloadTypeSerialized,
                        contentSerialized);
            }
            case 2 -> {
                Bytes enrsSerialized = SSZ.encodeBytesList(enrs);
                return Bytes.concatenate(
                        SSZ.encodeUInt8(getMessageType().ordinal()),
                        payloadTypeSerialized,
                        enrsSerialized);
            }
            default -> {
                throw new IllegalArgumentException("CONTENT: Invalid payload type");
            }
        }
    }
}
