package samba.domain.messages.response;

import java.util.Base64;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.*;
import samba.schema.ssz.containers.ContentContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Response message to Find Content (0x04).
 */
public class Content implements PortalWireMessage {

    private final int connectionId;
    private final Bytes content;
    private final List<String> enrs;
    private final int contentType;

    public Content(int connectionId) {
        this.contentType = 0;
        this.connectionId = connectionId;
        this.content = null;
        this.enrs = null;

    }

    public Content(Bytes content) {
        checkArgument(content.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Content size exceeds limit");
        this.contentType = 1;
        this.content = content;
        this.connectionId = 0;
        this.enrs = null;
    }

    public Content(List<String> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.length() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        this.contentType = 2;
        this.enrs = enrs;
        this.connectionId = 0;
        this.content = null;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }

    public int getPayloadType() {
        return contentType;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public Bytes getContent() {
        return content;
    }

    public List<String> getEnrList() {
        return enrs;
    }

    private List<Bytes> getEnrsBytes() {
        return enrs.stream().map(enr -> Bytes.wrap(Base64.getUrlDecoder().decode(enr))).toList();
    }

    private ContentContainer getContentContainer() {
        return switch (contentType) {
            case 0 -> new ContentContainer((byte) contentType, Bytes.ofUnsignedShort(connectionId));
            case 1 -> new ContentContainer((byte) contentType, content);
            case 2 -> new ContentContainer((byte) contentType, getEnrsBytes());
            default -> throw new AssertionError();
        };
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(),
            getContentContainer().sszSerialize());
    }
}
