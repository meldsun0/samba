package samba.domain.messages.response;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;

import static com.google.common.base.Preconditions.checkArgument;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

import samba.domain.messages.*;
import samba.schema.ssz.containers.PongContainer;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

/**
 * Response message to Ping(0x00)
 */
public class Pong implements PortalWireMessage {

    private final UInt64 enrSeq;
    private final Bytes customPayload;

    public Pong(UInt64 enrSeq, Bytes customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(customPayload.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Custom payload size exceeds limit");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PONG;
    }

    public Bytes getCustomPayload() {
        return customPayload;
    }

    public UInt64 getEnrSeq() {
        return enrSeq;
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new PongContainer(enrSeq, customPayload).sszSerialize());
    }
}
