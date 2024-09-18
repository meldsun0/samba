package samba.domain.messages.response;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.HistoryProtocolReceiveMessage;
import samba.domain.messages.MessageType;
import samba.domain.node.NodeId;


import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to Ping(0x00)
 */
public class Pong implements HistoryProtocolReceiveMessage {
    private final UInt64 enrSeq;
    private final byte[] customPayload;

    public Pong(UInt64 enrSeq, byte[] customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");

        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    public Pong() {
        this.enrSeq = null;
        this.customPayload = null;
    }

    public MessageType getMessageType() {
        return MessageType.PONG;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }

    public int getRadius(){
        return 0;
    }


    @Override
    public MessageType getType() {
        return MessageType.PONG;
    }

    @Override
    public Pong getDeserilizedMessage() {
        return this;
    }

    public NodeId getNodeId(){
        return null;
    }

    public  byte[] getCustomPayload(){
        return null;
    }
}
