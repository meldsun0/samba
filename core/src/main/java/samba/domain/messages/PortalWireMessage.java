package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;

public interface PortalWireMessage {

    int MAX_CUSTOM_PAYLOAD_BYTES = 2048;
    int MAX_DISTANCES = 256;
    int MAX_ENRS = 32;
    int MAX_KEYS = 64;

    MessageType getMessageType();

    Bytes getSszBytes();

    <T> T getMessage();

}