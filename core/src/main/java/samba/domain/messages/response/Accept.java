package samba.domain.messages.response;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.messages.ssz.containers.accept.AcceptContainerV0;
import samba.schema.messages.ssz.containers.accept.AcceptContainerV1;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/***
 * Response message to Offer (0x06).
 */
public class Accept implements PortalWireMessage {

  private final int connectionId;
  private final Bytes contentKeys;

  private final int protocolVersion;

  public Accept(int connectionId, Bytes contentKeys, int protocolVersion) {
    // content_keys limit 64
    this.connectionId = connectionId;
    this.contentKeys = contentKeys;
    this.protocolVersion = protocolVersion;
  }

  public static Accept fromSSZBytes(Bytes sszbytes, int protocolVersion) {
    Bytes container = sszbytes.slice(1);
    if (protocolVersion == 0) {
      AcceptContainerV0 acceptContainer = AcceptContainerV0.decodePacket(container);
      int connectionId = acceptContainer.getConnectionId();
      Bytes contentKeys = acceptContainer.getContentKeysBitList();
      if (contentKeys.size() > PortalWireMessage.MAX_KEYS / 8) {
        throw new IllegalArgumentException("ACCEPT: Number of content keys exceeds limit");
      }
      return new Accept(connectionId, contentKeys, protocolVersion);
    } else {
      AcceptContainerV1 acceptContainer = AcceptContainerV1.decodePacket(container);
      int connectionId = acceptContainer.getConnectionId();
      Bytes contentKeys = acceptContainer.getContentKeysByteList();
      if (contentKeys.size() > PortalWireMessage.MAX_KEYS) {
        throw new IllegalArgumentException("ACCEPT: Number of content keys exceeds limit");
      }
      return new Accept(connectionId, contentKeys, protocolVersion);
    }
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.ACCEPT;
  }

  public int getConnectionId() {
    return connectionId;
  }

  public Bytes getContentKeys() {
    return contentKeys;
  }

  public byte[] getContentKeysByteArray() {
    return contentKeys.toArray();
  }

  @Override
  public Bytes getSszBytes() {
    if (protocolVersion == 0) {
      return Bytes.concatenate(
          SszByte.of(getMessageType().getByteValue()).sszSerialize(),
          new AcceptContainerV0(Bytes.ofUnsignedShort(connectionId), contentKeys).sszSerialize());
    } else {
      return Bytes.concatenate(
          SszByte.of(getMessageType().getByteValue()).sszSerialize(),
          new AcceptContainerV1(Bytes.ofUnsignedShort(connectionId), contentKeys).sszSerialize());
    }
  }

  @Override
  public Accept getMessage() {
    return this;
  }

  public int getProtocolVersion() {
    return protocolVersion;
  }
}
