package samba.packet.impl;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt32;
import samba.packet.MessageTypeAndVersion;
import samba.type.Bytes16;
import samba.util.DecodeException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import static com.google.common.base.Preconditions.checkArgument;

public class Header extends AbstractBytes  {


  private static final int TYPE_VERSION_SIZE = 1;

  private MessageTypeAndVersion typeVersion;
  private Bytes connectionId;
  private UInt32 timestampMicroseconds;
  private UInt32 timestampDifferenceMicroseconds;
  private UInt32 windowSize;
  private Bytes sequenceNumber;
  private Bytes ackNumber;


  public Header(StaticHeader staticHeader, TAuthData authData) {
    super(Bytes.wrap(staticHeader.getBytes(), authData.getBytes()));
    checkArgument(
        authData.getBytes().size() == staticHeader.getAuthDataSize(),
        "Actual authData size doesn't match header auth-data-size field");
    this.staticHeader = staticHeader;
    this.authData = authData;
  }

  @Override
  public int getSize() {
    return StaticHeaderImpl.STATIC_HEADER_SIZE + getAuthDataBytes().size();
  }






  @Override
  public String toString() {
    return "Header{header=" + staticHeader + ", authData=" + authData + "}";
  }

  @Override
  public void validate() throws DecodeException {
      //TODO validate
  }
}
