package samba.message;

public class Packet {

    private PacketHeader header;
    private PacketHeaderExtension extension;
    private Byte[] payload;
}
