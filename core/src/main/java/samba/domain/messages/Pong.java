package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to Ping(0x00)
 */
public class Pong {
    private final UInt64 enrSeq;
    private final byte[] customPayload;

    public Pong(UInt64 enrSeq, byte[] customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    public MessageType getMessageType() {
        return MessageType.PONG;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}
