package samba.packet.impl;



public abstract class PacketImpl extends AbstractBytes implements Packet {

  @SuppressWarnings("unchecked")
  public static Packet<?> decrypt(Bytes data, Bytes16 iv, Bytes16 destNodeId)
      throws DecodeException {
    Header<?> header = HeaderImpl.decrypt(data, iv, destNodeId);
    Bytes messageData = data.slice(header.getSize());
    switch (header.getStaticHeader().getFlag()) {
      case WHOAREYOU:
        if (messageData.size() > 0) {
          throw new DecodeException("Non-empty message data for WHOAREYOU packet");
        }
        return new WhoAreYouPacketImpl((Header<WhoAreYouAuthData>) header);
      case MESSAGE:
        return new OrdinaryMessageImpl((Header<OrdinaryAuthData>) header, messageData);
      case HANDSHAKE:
        return new HandshakeMessagePacketImpl((Header<HandshakeAuthData>) header, messageData);
      default:
        throw new DecodeException("Unknown flag: " + header.getStaticHeader().getFlag());
    }
  }

  private final Header<TAuthData> header;
  private final Bytes messageBytes;

  protected PacketImpl(Header<TAuthData> header, Bytes cipheredMessageBytes) {
    super(Bytes.wrap(header.getBytes(), cipheredMessageBytes));
    this.header = header;
    this.messageBytes = cipheredMessageBytes;
  }

  @Override
  public Header<TAuthData> getHeader() {
    return header;
  }

  @Override
  public Bytes getMessageCyphered() {
    return messageBytes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "header="
        + header
        + ", cipherMsgSize="
        + getMessageCyphered().size()
        + '}';
  }
}
