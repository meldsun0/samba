package samba.domain.messages.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
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
import samba.schema.ssz.containers.ContentContainer;
import samba.schema.ssz.containers.FindContentContainer;
import samba.schema.ssz.containers.FindNodesContainer;
import samba.schema.ssz.containers.NodesContainer;
import samba.schema.ssz.containers.PingContainer;
import samba.schema.ssz.containers.PongContainer;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalWireMessageDecoder {

    public PortalWireMessage decode(NodeRecord srcNode, Bytes request) {
        PortalWireMessage parsedMessage = null;

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
            throw new IllegalArgumentException("Error parsing message: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing message: " + e.getMessage());
        }
    }

    private PortalWireMessage parsePing(Bytes request) {
        Bytes container = request.slice(1);
        PingContainer pingContainer = PingContainer.decodePacket(container);
        UInt64 enrSeq = pingContainer.getEnrSeq();
        Bytes customPayload = pingContainer.getCustomPayload();

        if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("PING: Custom payload size exceeds limit");
        }
        return new Ping(enrSeq, customPayload);
    }

    private PortalWireMessage parsePong(Bytes request) {
        Bytes container = request.slice(1);
        PongContainer pongContainer = PongContainer.decodePacket(container);
        UInt64 enrSeq = pongContainer.getEnrSeq();
        Bytes customPayload = pongContainer.getCustomPayload();

        if (customPayload.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("PONG: Custom payload size exceeds limit");
        }
        return new Pong(enrSeq, customPayload);
    }

    private PortalWireMessage parseFindNodes(Bytes request) {
        Bytes container = request.slice(1);
        FindNodesContainer findNodesContainer = FindNodesContainer.decodePacket(container);
        List<Integer> distances = findNodesContainer.getDistances();
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
        Bytes container = request.slice(1);
        NodesContainer nodesContainer = NodesContainer.decodePacket(container);
        int total = nodesContainer.getTotal();
        List<String> enrs = nodesContainer.getEnrs();

        if (total > 1) {
            throw new IllegalArgumentException("NODES: Total number of Nodes messages must be 1");
        }
        if (enrs.size() > PortalWireMessage.MAX_ENRS) {
            throw new IllegalArgumentException("NODES: Number of ENRs exceeds limit");
        }
        for (String enr : enrs) {
            if (enr.length() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("NODES: One or more ENRs exceed maximum payload size");
            }
        }
        // TODO: Remove requesting node (this node) from the list of ENRs
        // TODO: It is invalid to return multiple ENR records for the same node_id
        return new Nodes(enrs);
    }

    private PortalWireMessage parseFindContent(Bytes request) {
        Bytes container = request.slice(1);
        FindContentContainer findContentContainer = FindContentContainer.decodePacket(container);
        Bytes contentKey = findContentContainer.getContentKey();
        
        if (contentKey.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("FINDCONTENT: Content key size exceeds limit");
        }
        return new FindContent(contentKey);
    }

    private PortalWireMessage parseContent(Bytes request, NodeRecord srcNode) {
        Bytes container = request.slice(1);
        System.out.println("Test 0: " + container);
        ContentContainer contentContainer = ContentContainer.decodePacket(container);
        int contentType = contentContainer.getContentType();

        switch (contentType) {
            // uTP connection ID
            case 0 -> {
                System.out.println("Test 1");
                int connectionId = contentContainer.getConnectionId();
                System.out.println("Test 2");
                return new Content(connectionId);
            }
            // Requested content
            case 1 -> {
                Bytes content = contentContainer.getContent();
                if (content.size() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                    throw new IllegalArgumentException("CONTENT: Content size exceeds limit");
                }
                return new Content(content);
            }
            // ENRs
            case 2 -> {
                List<String> enrs = contentContainer.getEnrs();
                if (enrs.size() > PortalWireMessage.MAX_ENRS) {
                    throw new IllegalArgumentException("CONTENT: Number of ENRs exceeds limit");
                }
                for (String enr : enrs) {
                    if (enr.length() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE) {
                        throw new IllegalArgumentException("CONTENT: One or more ENRs exceed maximum payload size");
                    }
                }
                // TODO: Remove requesting node (this node) from the list of ENRs
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
        int connectionId = (request.slice(1, 3)).toInt();
        Bytes contentKeys = SSZ.decodeBytes(request.slice(3, request.size()));
        if (contentKeys.size() > PortalWireMessage.MAX_KEYS/8) {
            throw new IllegalArgumentException("ACCEPT: Number of content keys exceeds limit");
        }
        return new Accept(connectionId, contentKeys);
    }
}