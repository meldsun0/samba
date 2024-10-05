package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;
import samba.domain.messages.MessageType;

public interface PortalWireMessage {
    
    public final static int MAX_CUSTOM_PAYLOAD_SIZE = 2048;
    public final static int MAX_DISTANCES = 256;
    public static final int MAX_ENRS = 32;
    public static final int MAX_KEYS = 64;

    public MessageType getMessageType();

    public Bytes serialize();

    public <T>  T getMessage();
}