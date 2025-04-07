package samba.domain.messages.response;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.domain.types.unsigned.UInt16;
import samba.schema.messages.ssz.containers.PongContainer;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

/** Response message to Ping(0x00) */
public class Pong implements PortalWireMessage {

  private UInt64 enrSeq;
  private UInt16 payloadType;
  private Bytes payload;

  public Pong(UInt64 enrSeq, UInt16 payloadType, Bytes payload) {
    checkArgument(
        enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
    checkArgument(payloadType != null, "Payload type cannot be null");
    checkArgument(payload.size() <= MAX_EXTENSION_PAYLOAD_BYTES, "Payload size exceeds limit");
    this.enrSeq = enrSeq;
    this.payloadType = payloadType;
    this.payload = payload;
  }

  public Pong(org.apache.tuweni.units.bigints.UInt64 enrSeq, UInt16 payloadType, Bytes payload) {
    this(UInt64.valueOf(enrSeq.toBytes().toLong()), payloadType, payload);
  }

  public static Pong fromSSZBytes(Bytes sszbytes) {
    Bytes container = sszbytes.slice(1);
    PongContainer pongContainer = PongContainer.decodePacket(container);
    UInt64 enrSeq = pongContainer.getEnrSeq();
    UInt16 payloadType = pongContainer.getPayloadType();
    Bytes payload = pongContainer.getPayload();

    if (payload.size() > PortalWireMessage.MAX_EXTENSION_PAYLOAD_BYTES) {
      throw new IllegalArgumentException("PONG: Payload size exceeds limit");
    }
    return new Pong(enrSeq, payloadType, payload);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.PONG;
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

  public boolean containsPayload() {
    return !this.payload.isZero();
  }

  @Override
  public Bytes getSszBytes() {
    return Bytes.concatenate(
        SszByte.of(getMessageType().getByteValue()).sszSerialize(),
        new PongContainer(enrSeq, payloadType, payload).sszSerialize());
  }

  @Override
  public Pong getMessage() {
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
    Pong that = (Pong) o;
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
    return "Pong{"
        + "enrSeq="
        + enrSeq
        + ", payloadType="
        + payloadType
        + ", payload="
        + payload
        + '}';
  }
}
