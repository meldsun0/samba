package samba.domain.messages.processor;

import java.util.Base64;
import java.util.List;
import java.util.Random;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import samba.domain.messages.Accept;
import samba.domain.messages.Content;
import samba.domain.messages.FindContent;
import samba.domain.messages.FindNodes;
import samba.domain.messages.MessageType;
import samba.domain.messages.Nodes;
import samba.domain.messages.Offer;
import samba.domain.messages.Ping;
import samba.domain.messages.Pong;



public class PortalWireMessageDecoderTests {
    
    PortalWireMessageDecoder decoder;
    NodeRecord srcNode;

    @BeforeEach
    public void setUp() {
        this.decoder = new PortalWireMessageDecoder();
        this.srcNode = new NodeRecordBuilder()
            .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
            .address("12.34.45.67", Integer.parseInt("9001"))
            .build();
    }
    
@Test
    public void testParsePing() {
        Bytes pingBytes = Bytes.fromHexString("0x0001000000000000000c000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        Ping ping = (Ping) decoder.decode(srcNode, pingBytes);
        assertEquals(MessageType.PING, ping.getMessageType());
        assertEquals(UInt64.valueOf(1), ping.getEnrSeq());
        assertEquals(Bytes.fromHexString("0xfeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"), ping.getCustomPayload());
    }

    @Test
    public void testParsePong() {
        Bytes pongBytes = Bytes.fromHexString("0x0101000000000000000c000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f");
        Pong pong = (Pong) decoder.decode(srcNode, pongBytes);
        assertEquals(MessageType.PONG, pong.getMessageType());
        assertEquals(UInt64.valueOf(1), pong.getEnrSeq());
        assertEquals(Bytes.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"), pong.getCustomPayload());
    }

    @Test
    public void testParseFindNode() {
        Bytes findNodesBytes = Bytes.fromHexString("0x02040000000001ff00");
        FindNodes findNodes = (FindNodes) decoder.decode(srcNode, findNodesBytes);  
        List<Integer> distances = List.of(256, 255);
        assertEquals(MessageType.FIND_NODES, findNodes.getMessageType());
        assertEquals(distances, findNodes.getDistances());
    }

    @Test
    public void testParseNodesEmpty() {
        Bytes nodesBytes = Bytes.fromHexString("0x030105000000");
        Nodes nodes = (Nodes) decoder.decode(srcNode, nodesBytes);
        assertEquals(MessageType.NODES, nodes.getMessageType());
        assertEquals(1, nodes.getTotal().toInt());
        assertEquals(0, nodes.getEnrList().size());


    }

    @Test
    public void testParseNodesPopulated() {
        Bytes nodesBytes = Bytes
            .fromHexString("0x030105000000080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235");
        Nodes nodes = (Nodes) decoder.decode(srcNode, nodesBytes);
        List<Bytes> enrList = List.of(Bytes.wrap(Base64.getUrlDecoder().decode("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg")),
            Bytes.wrap(Base64.getUrlDecoder().decode("-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU")));
        assertEquals(MessageType.NODES, nodes.getMessageType());
        assertEquals(1, nodes.getTotal().toInt());
        assertEquals(enrList, nodes.getEnrList());

    }

    @Test
    public void testParseFindContent() {
        Bytes findContentBytes = Bytes.fromHexString("0x0404000000706f7274616c");
        FindContent findContent = (FindContent) decoder.decode(srcNode, findContentBytes);
        assertEquals(MessageType.FIND_CONTENT, findContent.getMessageType());
        assertEquals(Bytes.fromHexString("0x706f7274616c"), findContent.getContentKey());
    }

    @Test
    public void testParseContentConnectionId() {
        Bytes contentBytes = Bytes.fromHexString("0x05000102");
        Content content = (Content) decoder.decode(srcNode, contentBytes);
        assertEquals(MessageType.CONTENT, content.getMessageType());
        assertEquals(0, content.getPayloadType());
        assertEquals(Bytes.fromHexString("0x0102").toInt(), content.getConnectionId());
    }

    @Test
    public void testParseContentPayload() {
        Bytes contentBytes = Bytes.fromHexString("0x05017468652063616b652069732061206c6965");
        Content content = (Content) decoder.decode(srcNode, contentBytes);
        assertEquals(MessageType.CONTENT, content.getMessageType());
        assertEquals(1, content.getPayloadType());
        assertEquals(Bytes.fromHexString("0x7468652063616b652069732061206c6965"), content.getContent());
    }

    @Test
    public void testParseContentEnrs() {
        Bytes contentBytes = Bytes
            .fromHexString("0x0502080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235");
        Content content = (Content) decoder.decode(srcNode, contentBytes);
        List<Bytes> enrList = List.of(Bytes.wrap(Base64.getUrlDecoder().decode("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg")),
            Bytes.wrap(Base64.getUrlDecoder().decode("-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6)dLCUUp8xfVw50jU")));
        assertEquals(MessageType.CONTENT, content.getMessageType());
        assertEquals(2, content.getPayloadType());
        assertEquals(enrList, content.getEnrList());
    }

    @Test
    public void testParseOffer() {
        Bytes offerBytes = Bytes.fromHexString("0x060400000004000000010203");
        Offer offer = (Offer) decoder.decode(srcNode, offerBytes);
        List<Bytes> contentKeys = List.of(Bytes.fromHexString("0x010203"));
        assertEquals(MessageType.OFFER, offer.getMessageType());
        assertEquals(contentKeys, offer.getContentKeys());
    }

    @Test
    public void testParseAccept() {
        Bytes acceptBytes = Bytes.fromHexString("0x070102060000000101");
        Accept accept = (Accept) decoder.decode(srcNode, acceptBytes);
        int connectionId = Bytes.fromHexString("0x0102").toInt();
        Bytes contentKeys = Bytes.fromHexString("0x01");
        assertEquals(MessageType.ACCEPT, accept.getMessageType());
        assertEquals(connectionId, accept.getConnectionId());
        assertEquals(contentKeys, accept.getContentKeys());
    }
}