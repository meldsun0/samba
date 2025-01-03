package samba.message;

public class Packet {

    private PacketHeader header;
    private PacketHeaderExtension extension;
    private Byte[] payload;


    public int getPacketLength() {
        return this.header.getLength() + this.extension.getLength() + payload.length;
    }
}
