package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;

public interface PortalWireMessage {
    
    public int MAX_CUSTOM_PAYLOAD_SIZE = 2048;

    MessageType getMessageType();
}