package samba.packet.impl;


import org.apache.tuweni.bytes.Bytes;
import samba.packet.RawPacket;


public class RawPacketImpl extends AbstractBytes implements RawPacket {
  private static final int MASKING_IV_SIZE = 16;


  public static RawPacket create(Bytes data) {
    return new RawPacketImpl(data);
  }

  public RawPacketImpl(Bytes bytes) {
    super(checkMinSize(bytes, MASKING_IV_SIZE));
  }


}
