package samba.domain.messages.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.Accept;
import samba.domain.messages.Content;
import samba.domain.messages.FindContent;
import samba.domain.messages.FindNodes;
import samba.domain.messages.MessageType;
import samba.domain.messages.Nodes;
import samba.domain.messages.Offer;
import samba.domain.messages.Ping;
import samba.domain.messages.Pong;
import samba.domain.messages.PortalWireMessage;

public class PortalWireMessageDecoder {

    public PortalWireMessage decode(NodeRecord srcNode, Bytes request) {
        PortalWireMessage parsedMessage = null;
        // Parse Bytes to obtain message type
        int packetType = SSZ.decodeInt8(request.slice(0, 1));
        try {
            MessageType messageType = MessageType.fromInt(packetType);
            switch (messageType) {
                case MessageType.PING -> parsedMessage = parsePing(request);
                case MessageType.PONG -> parsedMessage = parsePong(request);
                case MessageType.FIND_NODES -> parsedMessage = parseFindNodes(request);
                case MessageType.NODES -> parsedMessage = parseNodes(request, srcNode);
                case MessageType.FIND_CONTENT -> parsedMessage = parseFindContent(request);
                case MessageType.CONTENT -> parsedMessage = parseContent(request, srcNode);
                case MessageType.OFFER -> parsedMessage = parseOffer(request);
                case MessageType.ACCEPT -> parsedMessage = parseAccept(request);
                default -> throw new AssertionError();
            }
            return parsedMessage;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown message type: " + packetType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing message: " + e.getMessage());
        }

        // Parse Bytes to obtain message
        // Put message into apprpriate packet type and return packet
        // Error if packet type is not recognized
    }

    private PortalWireMessage parsePing(Bytes request) {
        UInt64 enrSeq = UInt64.valueOf(SSZ.decodeUInt64(request.slice(1, 9)));
        Bytes customPayload = SSZ.decodeBytes(request.slice(9, request.size()));
        if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("PING: Custom payload size exceeds limit");
        }
        return new Ping(enrSeq, customPayload);
    }

    private PortalWireMessage parsePong(Bytes request) {
        UInt64 enrSeq = UInt64.valueOf(SSZ.decodeUInt64(request.slice(1, 9)));
        Bytes customPayload = SSZ.decodeBytes(request.slice(9, request.size()));
        if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("PONG: Custom payload size exceeds limit");
        }
        return new Pong(enrSeq, customPayload);
    }

    private PortalWireMessage parseFindNodes(Bytes request) {
        List<Integer> distances = SSZ.decodeUInt16List(request.slice(1, request.size()));
        Set<Integer> uniqueDistances = new HashSet<>(distances);
        distances.clear();
        distances.addAll(uniqueDistances);
        if (distances.size() > PortalWireMessage.MAX_DISTANCES) {
            throw new IllegalArgumentException("FINDNODES: Number of distances exceeds limit");
        } else {
            for (Integer distance : distances) {
                if (distance < 0 || distance > 256) {
                    throw new IllegalArgumentException("FINDNODES: Distance must be within the inclusive range [0,256]");
                }
            }
        }
        return new FindNodes(distances);
    }

    private PortalWireMessage parseNodes(Bytes request, NodeRecord srcNode) {
        int total = SSZ.decodeUInt8(request.slice(1, 2));
        List<Bytes> enrs = SSZ.decodeBytesList(request.slice(2, request.size()));
        if (total > 1) {
            throw new IllegalArgumentException("NODES: Total number of Nodes messages must be 1");
        }
        if (enrs.size() > PortalWireMessage.MAX_ENRS) {
            throw new IllegalArgumentException("NODES: Number of ENRs exceeds limit");
        }
        for (Bytes enr : enrs) {
            if (enr.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("NODES: One or more ENRs exceed maximum payload size");
            }
        }
        // TODO: Remove requesting node (this node) from the list of ENRs
        return new Nodes(enrs);
        // It is invalid to return multiple ENR records for the same node_id
    }

    private PortalWireMessage parseFindContent(Bytes request) {
        Bytes contentKey = SSZ.decodeBytes(request.slice(9, request.size()));
        if (contentKey.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("FINDCONTENT: Content key size exceeds limit");
        }
        return new FindContent(contentKey);
    }

    private PortalWireMessage parseContent(Bytes request, NodeRecord srcNode) {
        int payloadType = SSZ.decodeInt8(request.slice(1, 2));
        switch (payloadType) {
            // uTP connection ID
            case 0 -> {
                UInt64 connectionId = UInt64.valueOf(SSZ.decodeUInt64(request.slice(2, 4)));
                return new Content(connectionId);
            }
            // Requested content
            case 1 -> {
                Bytes content = SSZ.decodeBytes(request.slice(2, request.size()));
                if (content.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                    throw new IllegalArgumentException("CONTENT: Content size exceeds limit");
                }
                return new Content(content);
            }
            // ENRs
            case 2 -> {
                List<Bytes> enrs = SSZ.decodeBytesList(request.slice(2, request.size()));
                if (enrs.size() > PortalWireMessage.MAX_ENRS) {
                    throw new IllegalArgumentException("CONTENT: Number of ENRs exceeds limit");
                }
                for (Bytes enr : enrs) {
                    if (enr.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                        throw new IllegalArgumentException("CONTENT: One or more ENRs exceed maximum payload size");
                    }
                }
                enrs.removeIf(enr -> enr.equals(srcNode.asRlp()));
                // TODO: Remove requesting node (this node) from the list of ENRs
                return new Content(enrs);
            }
            default -> {
                throw new IllegalArgumentException("CONTENT: Invalid payload type");
            }
        }
    }

    private PortalWireMessage parseOffer(Bytes request) {
        List<Bytes> contentKeys = SSZ.decodeBytesList(request.slice(1, request.size()));
        if (contentKeys.size() > PortalWireMessage.MAX_KEYS) {
            throw new IllegalArgumentException("OFFER: Number of content keys exceeds limit");
        }
        for (Bytes key : contentKeys) {
            if (key.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("OFFER: One or more content keys exceed maximum payload size");
            }
        }
        return new Offer(contentKeys);
    }

    private PortalWireMessage parseAccept(Bytes request) {
        UInt64 connectionId = UInt64.valueOf(SSZ.decodeUInt64(request.slice(1, 3)));
        Bytes contentKeys = SSZ.decodeBytes(request.slice(3, request.size()));
        if (contentKeys.size() > PortalWireMessage.MAX_KEYS/8) {
            throw new IllegalArgumentException("ACCEPT: Number of content keys exceeds limit");
        }
        return new Accept(connectionId, contentKeys);
        // Parse Bytes to obtain message
        // Put message into apprpriate packet type and return packet
    }
}