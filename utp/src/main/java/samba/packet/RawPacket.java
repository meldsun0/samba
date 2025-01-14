package samba.packet;

import org.apache.tuweni.bytes.Bytes;
import samba.util.DecodeException;


public interface RawPacket extends BytesSerializable {

  @Override
  default void validate() throws DecodeException {
    //TODO
  }
}
