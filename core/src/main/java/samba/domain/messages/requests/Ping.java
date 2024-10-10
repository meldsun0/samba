package samba.domain.messages.requests;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import org.ethereum.beacon.discovery.message.MessageCode;
import org.ethereum.beacon.discovery.message.PingMessage;
import samba.domain.messages.*;
import samba.schema.ssz.containers.PingContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

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

    public static Ping fromSSZBytes(Bytes sszbytes){
            Bytes container = sszbytes.slice(1);
            PingContainer pingContainer = PingContainer.decodePacket(container);
            UInt64 enrSeq = pingContainer.getEnrSeq();
            Bytes customPayload = pingContainer.getCustomPayload();

            if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("PING: Custom payload size exceeds limit"); //TODO change exception
            }
            return new Ping(enrSeq, customPayload);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PING;
    }

    public Bytes getCustomPayload() {
        return customPayload;
    }

    public UInt64 getEnrSeq() {
        return enrSeq;
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new PingContainer(enrSeq, customPayload).sszSerialize());
    }

    @Override
    public Ping getMessage() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ping that = (Ping) o;
        return  Objects.equal(enrSeq, that.enrSeq) && Objects.equal(customPayload, that.customPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enrSeq, customPayload);
    }

    @Override
    public String toString() {
        return "Ping{" + "enrSeq=" + enrSeq + ", customPayload=" + customPayload + '}';
    }
}
