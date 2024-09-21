package samba.domain.messages.requests;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.MessageType;
import samba.domain.messages.HistoryProtocolRequestMessage;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to check if a node is reachable, communicate basic information about our node,
 * and request basic information about the recipient node.
 */
public class Ping implements HistoryProtocolRequestMessage {

    private final UInt64 enrSeq;
    private final byte[] customPayload;

    public Ping(UInt64 enrSeq, byte[] customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }

    @Override
    public Bytes getMessageInBytes() {
        return null;
    }

    @Override
    public MessageType getType() {
        return MessageType.PING;
    }

    @Override
    public Bytes getSSZMessageInBytes() {
        return Bytes.EMPTY;
    }
}