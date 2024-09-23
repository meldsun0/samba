package samba.domain.messages.response;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.HistoryProtocolMessage;
import samba.domain.messages.MessageType;
import samba.domain.node.NodeId;


import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * Response message to Ping(0x00)
 */
public class Pong implements HistoryProtocolMessage {

//    private final UInt64 enrSeq;
//    private final Bytes customPayload;
    private NodeRecord nodeRecord;

    public Pong(NodeRecord node) {
      //  checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        //  checkArgument(customPayload.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Custom payload size exceeds limit");

//        this.enrSeq = enrSeq;
//        this.customPayload = customPayload;
        this.nodeRecord = node;
    }

    public Bytes getCustomPayload() {
        return Bytes.EMPTY;
    }

    public NodeRecord getNodeRecord(){
        return this.nodeRecord;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(nodeRecord.getSeq());
    }

    @Override
    public Bytes getMessageInBytes() {
        Bytes enrSeqSerialized = SSZ.encodeUInt64(nodeRecord.getSeq().toLong());
        Bytes customPayloadSerialized = SSZ.encodeBytes(Bytes.EMPTY);
        return Bytes.concatenate(
                SSZ.encodeUInt8(getType().ordinal()),
                enrSeqSerialized,
                customPayloadSerialized);
    }

    @Override
    public Bytes getSSZMessageInBytes() {
        return null;
    }

    @Override
    public MessageType getType() {
        return MessageType.PONG;
    }

    @Override
    public Pong getMessage() {
        return this;
    }

    public NodeId getNodeId() {
           return new NodeId() {
               @Override
               public Bytes toBytes() {
                   return  nodeRecord.getNodeId();
               }
           };
    }
}