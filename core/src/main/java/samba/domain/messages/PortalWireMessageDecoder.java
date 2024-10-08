package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.*;
import samba.domain.messages.response.Accept;
import samba.domain.messages.response.Content;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;

public class PortalWireMessageDecoder {

    public static PortalWireMessage decode(NodeRecord sourceNode, Bytes sszbytes) { //TODO change NodeRecord
        int packetType = SSZ.decodeInt8(sszbytes.slice(0, 1));
        MessageType messageType = MessageType.fromInt(packetType);
        if (messageType == null) {
            throw new IllegalArgumentException("Invalid message type from int: " + packetType); //TODO build own runtime exception.
        }
        return switch (messageType) {
            case PING -> Ping.fromSSZBytes(sszbytes);
            case PONG -> Pong.fromSSZBytes(sszbytes);
            case FIND_NODES -> FindNodes.fromSSZBytes(sszbytes);
            case NODES -> Nodes.fromSSZBytes(sszbytes, sourceNode);
            case FIND_CONTENT -> FindContent.fromSSZBytes(sszbytes);
            case CONTENT -> Content.fromSSZBytes(sszbytes, sourceNode);
            case OFFER -> Offer.fromSSZBytes(sszbytes);
            case ACCEPT -> Accept.fromSSZBytes(sszbytes);
            default ->
                    throw new RuntimeException(String.format("Creation of a PortalWireMessage from messageType %s is not supported", messageType)); //TODO build own runtime exception.
        };
    }
}