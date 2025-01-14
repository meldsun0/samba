package samba.packet.impl;

import org.apache.tuweni.bytes.Bytes;
import samba.util.DecodeException;

public class Packet extends AbstractBytes {

    private Header header;
    private HeaderExtension extension;
    private Byte[] payload;

    protected Packet(Bytes bytes) {
        super(Bytes.EMPTY);
    }


    @Override
    public void validate() throws DecodeException {

    }
}
