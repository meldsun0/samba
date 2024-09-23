package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;


public interface HistoryProtocolMessage {

    public MessageType getType();

    public Bytes getMessageInBytes();

    public Bytes getSSZMessageInBytes();

    public  <T>  T getMessage();
}
