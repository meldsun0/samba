package samba.domain.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Offer;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Accept;
import samba.domain.messages.response.Content;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalWireMessageDecoderTests {

  NodeRecord srcNode;

  @BeforeEach
  public void setUp() {
    this.srcNode =
        new NodeRecordBuilder()
            .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
            .address("12.34.45.67", Integer.parseInt("9001"))
            .build();
  }

  @Test
  public void testParsePing() {
    Bytes pingBytes = Bytes.fromHexString("0x00010000000000000000000e0000001234");
    Ping ping = (Ping) PortalWireMessageDecoder.decode(srcNode, pingBytes, 0);
    assertEquals(MessageType.PING, ping.getMessageType());
    assertEquals(UInt64.valueOf(1), ping.getEnrSeq());
    assertEquals(UInt16.ZERO, ping.getPayloadType());
    assertEquals(Bytes.fromHexString("0x1234"), ping.getPayload());
  }

  @Test
  public void testParsePong() {
    Bytes pongBytes = Bytes.fromHexString("0x01010000000000000000000e0000001234");
    Pong pong = (Pong) PortalWireMessageDecoder.decode(srcNode, pongBytes, 0);
    assertEquals(MessageType.PONG, pong.getMessageType());
    assertEquals(UInt64.valueOf(1), pong.getEnrSeq());
    assertEquals(UInt16.ZERO, pong.getPayloadType());
    assertEquals(Bytes.fromHexString("0x1234"), pong.getPayload());
  }

  @Test
  public void testParseFindNode() {
    Bytes findNodesBytes = Bytes.fromHexString("0x02040000000001ff00");
    FindNodes findNodes = (FindNodes) PortalWireMessageDecoder.decode(srcNode, findNodesBytes, 0);
    Set<Integer> distances = Set.of(256, 255);
    assertEquals(MessageType.FIND_NODES, findNodes.getMessageType());
    assertEquals(distances, findNodes.getDistances());
  }

  @Test
  public void testParseNodesEmpty() {
    Bytes nodesBytes = Bytes.fromHexString("0x030105000000");
    Nodes nodes = (Nodes) PortalWireMessageDecoder.decode(srcNode, nodesBytes, 0);
    assertEquals(MessageType.NODES, nodes.getMessageType());
    assertEquals(1, nodes.getTotal());
    assertEquals(0, nodes.getEnrList().size());
  }

  @Test
  public void testParseNodesPopulated() {
    Bytes nodesBytes =
        Bytes.fromHexString(
            "0x030105000000080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235");
    Nodes nodes = (Nodes) PortalWireMessageDecoder.decode(srcNode, nodesBytes, 0);
    List<String> enrList =
        List.of(
            "-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg",
            "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");
    assertEquals(MessageType.NODES, nodes.getMessageType());
    assertEquals(1, nodes.getTotal());
    assertEquals(
        enrList,
        nodes.getEnrList().stream().map(enr -> enr.replace("=", "")).collect(Collectors.toList()));
  }

  @Test
  public void testParseFindContent() {
    Bytes findContentBytes = Bytes.fromHexString("0x0404000000706f7274616c");
    FindContent findContent =
        (FindContent) PortalWireMessageDecoder.decode(srcNode, findContentBytes, 0);
    assertEquals(MessageType.FIND_CONTENT, findContent.getMessageType());
    assertEquals(Bytes.fromHexString("0x706f7274616c"), findContent.getContentKey());
  }

  @Test
  public void testParseContentConnectionId() {
    Bytes contentBytes = Bytes.fromHexString("0x05000102");
    Content content = (Content) PortalWireMessageDecoder.decode(srcNode, contentBytes, 0);
    assertEquals(MessageType.CONTENT, content.getMessageType());
    assertEquals(0, content.getContentType());
    assertEquals(Bytes.fromHexString("0x0102").toInt(), content.getConnectionId());
  }

  @Test
  public void testParseContentPayload() {
    Bytes contentBytes = Bytes.fromHexString("0x05017468652063616b652069732061206c6965");
    Content content = (Content) PortalWireMessageDecoder.decode(srcNode, contentBytes, 0);
    assertEquals(MessageType.CONTENT, content.getMessageType());
    assertEquals(1, content.getContentType());
    assertEquals(Bytes.fromHexString("0x7468652063616b652069732061206c6965"), content.getContent());
  }

  @Test
  public void testParseContentEnrs() {
    Bytes contentBytes =
        Bytes.fromHexString(
            "0x0502080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235");
    Content content = (Content) PortalWireMessageDecoder.decode(srcNode, contentBytes, 0);
    List<String> enrList =
        List.of(
            "-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg",
            "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU");
    assertEquals(MessageType.CONTENT, content.getMessageType());
    assertEquals(2, content.getContentType());
    assertEquals(
        enrList,
        content.getEnrList().stream()
            .map(enr -> enr.replace("=", ""))
            .collect(Collectors.toList()));
  }

  @Test
  public void testParseOffer() {
    Bytes offerBytes = Bytes.fromHexString("0x060400000004000000010203");
    Offer offer = (Offer) PortalWireMessageDecoder.decode(srcNode, offerBytes, 0);
    List<Bytes> contentKeys = List.of(Bytes.fromHexString("0x010203"));
    assertEquals(MessageType.OFFER, offer.getMessageType());
    assertEquals(contentKeys, offer.getContentKeys());
  }

  @Test
  public void testParseAccept() {
    Bytes acceptBytes = Bytes.fromHexString("0x070102060000000101");
    Accept accept = (Accept) PortalWireMessageDecoder.decode(srcNode, acceptBytes, 0);
    int connectionId = Bytes.fromHexString("0x0102").toInt();
    Bytes contentKeys = Bytes.fromHexString("0x01");
    assertEquals(MessageType.ACCEPT, accept.getMessageType());
    assertEquals(connectionId, accept.getConnectionId());
    assertEquals(contentKeys, accept.getContentKeys());
  }
}
