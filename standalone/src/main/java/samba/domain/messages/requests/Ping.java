package samba.domain.messages.requests;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.types.unsigned.UInt16;
import samba.schema.messages.ssz.containers.PingContainer;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

/**
 * Request message to check if a node is reachable, communicate basic information about our node,
 * and request basic information about the recipient node.
 */
public class Ping implements PortalWireMessage {

  private final UInt64 enrSeq;
  private final UInt16 payloadType;
  private final Bytes payload;

  public Ping(UInt64 enrSeq, UInt16 payloadType, Bytes payload) {
    checkArgument(
        enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
    checkArgument(payloadType != null, "Payload type cannot be null");
    checkArgument(MAX_EXTENSION_PAYLOAD_BYTES > payload.size(), "Payload size exceeds limit");

    this.enrSeq = enrSeq;
    this.payloadType = payloadType;
    this.payload = payload;
  }

  public Ping(org.apache.tuweni.units.bigints.UInt64 enrSeq, UInt16 payloadType, Bytes payload) {
    this(UInt64.valueOf(enrSeq.toBytes().toLong()), payloadType, payload);
  }

  public static Ping fromSSZBytes(Bytes sszbytes) {
    Bytes container = sszbytes.slice(1);
    PingContainer pingContainer = PingContainer.decodePacket(container);
    UInt64 enrSeq = pingContainer.getEnrSeq();
    UInt16 payloadType = pingContainer.getPayloadType();
    Bytes payload = pingContainer.getPayload();

    if (payload.size() > PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES) {
      throw new IllegalArgumentException(
          "PING: Custom payload size exceeds limit"); // TODO change exception
    }
    return new Ping(enrSeq, payloadType, payload);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.PING;
  }

  public UInt64 getEnrSeq() {
    return enrSeq;
  }

  public UInt16 getPayloadType() {
    return payloadType;
  }

  public Bytes getPayload() {
    return payload;
  }

  @Override
  public Bytes getSszBytes() {
    return Bytes.concatenate(
        SszByte.of(getMessageType().getByteValue()).sszSerialize(),
        new PingContainer(enrSeq, payloadType, payload).sszSerialize());
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
    return Objects.equal(enrSeq, that.enrSeq)
        && Objects.equal(payloadType, that.payloadType)
        && Objects.equal(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(enrSeq, payloadType, payload);
  }

  @Override
  public String toString() {
    return "Ping{"
        + "enrSeq="
        + enrSeq
        + ", payloadType="
        + payloadType
        + ", payload="
        + payload
        + '}';
  }
}
