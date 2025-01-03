package samba.packet;

import org.apache.tuweni.bytes.Bytes;
import samba.util.DecodeException;

public interface BytesSerializable {

    void validate() throws DecodeException;

    Bytes getBytes();
}
