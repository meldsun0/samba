/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.packet;

import org.apache.tuweni.bytes.Bytes;
import samba.packet.impl.RawPacketImpl;
import samba.util.DecodeException;


/**
 * Raw packet with encrypted (AES/CTR) header
 *
 * <p>{@code packet = masking-iv || masked-header || message }
 *
 * <p>The ciphered raw packet can extract just {@code masking-iv } field until decrypted
 */
public interface RawPacket extends BytesSerializable {


  static RawPacket decode(Bytes data) throws DecodeException {
    RawPacket rawPacket = RawPacketImpl.create(data);
    rawPacket.validate();
    return rawPacket;
  }


  @Override
  default void validate() throws DecodeException {
//    DecodeException.wrap(
//        () -> "Couldn't decode IV: " + getBytes(),
//        () -> {
//          getMaskingIV();
//        });
  }
}
