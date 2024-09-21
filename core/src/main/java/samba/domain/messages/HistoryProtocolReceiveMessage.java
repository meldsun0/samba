package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;


public interface HistoryProtocolReceiveMessage {


    public MessageType getType();

    public <T> T getDeserilizedMessage();

    public MessageType getMessageType();

    public Bytes serialize();

}
