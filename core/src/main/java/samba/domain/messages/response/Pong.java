package samba.domain.messages.response;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.requests.Ping;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

import samba.domain.messages.*;
import samba.schema.ssz.containers.PongContainer;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.node.NodeId;


/**
 * Response message to Ping(0x00)
 */
public class Pong implements PortalWireMessage {

    private UInt64 enrSeq;
    private Bytes customPayload;
    private NodeRecord nodeRecord;

    public Pong(NodeRecord node) {
        this.nodeRecord = node;
    }

    public Pong(UInt64 enrSeq, Bytes customPayload) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(customPayload.size() <= MAX_CUSTOM_PAYLOAD_SIZE, "Custom payload size exceeds limit");
        this.enrSeq = enrSeq;
        this.customPayload = customPayload;
    }

    public static Pong fromSSZBytes(Bytes sszbytes){
        Bytes container = sszbytes.slice(1);
        PongContainer pongContainer = PongContainer.decodePacket(container);
        UInt64 enrSeq = pongContainer.getEnrSeq();
        Bytes customPayload = pongContainer.getCustomPayload();

        if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("PONG: Custom payload size exceeds limit");
        }
        return new Pong(enrSeq, customPayload);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PONG;
    }

    public Bytes getCustomPayload() {
        return customPayload;
    }

    public UInt64 getEnrSeq() {
        return enrSeq;
    }

    public NodeRecord getNodeRecord() {
        return this.nodeRecord;
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new PongContainer(enrSeq, customPayload).sszSerialize());
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

