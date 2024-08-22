package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to check if a node is reachable, communicate basic information about our node,
 * and request basic information about the recipient node.
 */
public class Ping {
    private final UInt64 enrSeq;
    private final byte[] customPayload;

    public Ping(UInt64 enrSeq, byte[] customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    public MessageType getMessageType() {
        return MessageType.PING;
    }
    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}