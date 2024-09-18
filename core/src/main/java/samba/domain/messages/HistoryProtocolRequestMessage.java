package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;


public interface HistoryProtocolRequestMessage {

    Bytes getMessageInBytes();

    MessageType getType();

    Bytes getSSZMessageInBytes();
}
