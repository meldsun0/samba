package samba.domain.messages.response;

import java.util.Base64;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
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
        checkArgument(content.size() <= MAX_CUSTOM_PAYLOAD_BYTES, "Content size exceeds limit");
        this.contentType = 1;
        this.content = content;
        this.connectionId = 0;
        this.enrs = null;
    }

    public Content(List<String> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.length() <= MAX_CUSTOM_PAYLOAD_BYTES), "One or more ENRs exceed maximum payload size");
        this.contentType = 2;
        this.enrs = enrs;
        this.connectionId = 0;
        this.content = null;
    }

    public static Content fromSSZBytes(Bytes sszbytes, NodeRecord srcNode) {
        Bytes container = sszbytes.slice(1);
        ContentContainer contentContainer = ContentContainer.decodePacket(container);
        int contentType = contentContainer.getContentType();

        switch (contentType) {
            // uTP connection ID
            case 0 -> {
                int connectionId = contentContainer.getConnectionId();
                if (connectionId < 0) {
                    throw new IllegalArgumentException("CONTENT: Connection ID must be non-negative");
                } else if (connectionId > 65535) { // 2^16 - 1
                    throw new IllegalArgumentException("CONTENT: Connection ID exceeds maximum value");
                }
                return new Content(connectionId);
            }
            // Requested content
            case 1 -> {
                Bytes content = contentContainer.getContent();
                if (content.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
                    throw new IllegalArgumentException("CONTENT: Content size exceeds limit");
                } else if (content.size() == 0) {
                    throw new IllegalArgumentException("CONTENT: Content must not be empty");
                }
                return new Content(content);
            }
            // ENRs
            case 2 -> {
                List<String> enrs = contentContainer.getEnrs();
                if (enrs.size() > PortalWireMessage.MAX_ENRS) {
                    throw new IllegalArgumentException("CONTENT: Number of ENRs exceeds limit");
                }
                for (String enr : enrs) {
                    if (enr.length() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
                        throw new IllegalArgumentException("CONTENT: One or more ENRs exceed maximum payload size");
                    }
                }
                // TODO: Remove requesting node (this node) from the list of ENRs
                return new Content(enrs);
            }
            default -> {
                throw new IllegalArgumentException("CONTENT: Invalid payload type");
            }
        }
    }


    @Override
    public MessageType getMessageType() {
        return MessageType.CONTENT;
    }

    public int getContentType() {
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
    public Bytes getSszBytes() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                getContentContainer().sszSerialize());
    }

    @Override
    public Content getMessage() {
        return this;
    }
}
