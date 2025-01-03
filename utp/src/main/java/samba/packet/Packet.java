package samba.packet;

import samba.packet.impl.Header;
import samba.util.DecodeException;


public interface Packet extends BytesSerializable {

    Header getHeader();

    @Override
    default void validate() throws DecodeException {
        getHeader().validate();
    }
}
