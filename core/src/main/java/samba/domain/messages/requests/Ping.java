package samba.domain.messages.requests;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to check if a node is reachable, communicate basic information about our node,
 * and request basic information about the recipient node.
 */
public class Ping implements PortalWireMessage {

    private final UInt64 enrSeq;
    private final Bytes customPayload;

    public Ping(UInt64 enrSeq, Bytes customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(customPayload.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Custom payload size exceeds limit");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PING;
    }

    public Bytes getCustomPayload() {
        return customPayload;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }

    @Override
    public Bytes serialize() {
        Bytes enrSeqSerialized = SSZ.encodeUInt64(enrSeq.toLong());
        Bytes customPayloadSerialized = SSZ.encodeBytes(customPayload);
        return Bytes.concatenate(
                SSZ.encodeUInt8(getMessageType().ordinal()),
                enrSeqSerialized,
                customPayloadSerialized);
    }

    @Override
    public Ping getMessage() {
        return this;
    }
}
