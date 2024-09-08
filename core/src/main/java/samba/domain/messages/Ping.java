package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to check if a node is reachable, communicate basic information about our node,
 * and request basic information about the recipient node.
 */
public class Ping implements PortalWireMessage {

    private final UInt64 enrSeq;
    private final Byte[] customPayload;

    public Ping(UInt64 enrSeq, Byte[] customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(customPayload.length <= MAX_CUSTOM_PAYLOAD_SIZE, "Custom payload size exceeds limit");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PING;
    }

    public Byte[] getCustomPayload() {
        return customPayload;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}