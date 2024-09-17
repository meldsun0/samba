package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;


public interface ProtocolMessage {

    Bytes getMessageInBytes();

    MessageType getType();

    Bytes getSSZMessageInBytes();
}
