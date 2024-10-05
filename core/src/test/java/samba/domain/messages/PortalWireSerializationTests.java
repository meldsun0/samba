package samba.domain.messages;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalWireSerializationTests {

    @Test
    public void testPingSerialization() {
        Ping ping = new Ping(UInt64.valueOf(1), Bytes.fromHexString("0xfeffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        Bytes serialized = ping.serialize();
        assertEquals(Bytes.fromHexString("0x0001000000000000000c000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"), serialized);
    }

    @Test
    public void testPongSerialization() {
        Pong pong = new Pong(UInt64.valueOf(1), Bytes.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"));
        Bytes serialized = pong.serialize();
        assertEquals(Bytes.fromHexString("0x0101000000000000000c000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"), serialized);
    }

    @Test
    public void testFindNodeSerialization() {
        FindNodes findNodes =  new FindNodes(List.of(256, 255));
        Bytes serialized = findNodes.serialize();
        assertEquals(Bytes.fromHexString("0x02040000000001ff00"), serialized);
    }

    @Test
    public void testNodesSerializationEmpty() {
        List<String> enrs = List.of();
        Nodes nodes = new Nodes(enrs);
        Bytes serialized = nodes.serialize();
        assertEquals(Bytes.fromHexString("0x030105000000"), serialized);
    }

    @Test
    public void testNodesSerializationPopulated() {
        Nodes nodes = new Nodes(List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg",
            "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU"));
        Bytes serialized = nodes.serialize();
        assertEquals(Bytes.fromHexString("0x030105000000080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235"),
            serialized);
    }

    @Test
    public void testFindContentSerialization() {
        FindContent findContent = new FindContent(Bytes.fromHexString("0x706f7274616c"));
        Bytes serialized = findContent.serialize();
        assertEquals(Bytes.fromHexString("0x0404000000706f7274616c"), serialized);
    }

    @Test
    public void testContentSerializationConnectionId() {
        Content content = new Content(Bytes.fromHexString("0x0102").toInt());
        Bytes serialized = content.serialize();
        assertEquals(Bytes.fromHexString("0x05000102"), serialized);
    }

    @Test
    public void testContentSerializationPayload() {
        Content content = new Content(Bytes.fromHexString("0x7468652063616b652069732061206c6965"));
        Bytes serialized = content.serialize();
        assertEquals(Bytes.fromHexString("0x05017468652063616b652069732061206c6965"), serialized);
    }

    @Test
    public void testContentSerializationEnrs() {
        Content content = new Content(List.of("-HW4QBzimRxkmT18hMKaAL3IcZF1UcfTMPyi3Q1pxwZZbcZVRI8DC5infUAB_UauARLOJtYTxaagKoGmIjzQxO2qUygBgmlkgnY0iXNlY3AyNTZrMaEDymNMrg1JrLQB2KTGtv6MVbcNEVv0AHacwUAPMljNMTg",
            "-HW4QNfxw543Ypf4HXKXdYxkyzfcxcO-6p9X986WldfVpnVTQX1xlTnWrktEWUbeTZnmgOuAY_KUhbVV1Ft98WoYUBMBgmlkgnY0iXNlY3AyNTZrMaEDDiy3QkHAxPyOgWbxp5oF1bDdlYE6dLCUUp8xfVw50jU"));
        Bytes serialized = content.serialize();
        assertEquals(Bytes.fromHexString("0x0502080000007f000000f875b8401ce2991c64993d7c84c29a00bdc871917551c7d330fca2dd0d69c706596dc655448f030b98a77d4001fd46ae0112ce26d613c5a6a02a81a6223cd0c4edaa53280182696482763489736563703235366b31a103ca634cae0d49acb401d8a4c6b6fe8c55b70d115bf400769cc1400f3258cd3138f875b840d7f1c39e376297f81d7297758c64cb37dcc5c3beea9f57f7ce9695d7d5a67553417d719539d6ae4b445946de4d99e680eb8063f29485b555d45b7df16a1850130182696482763489736563703235366b31a1030e2cb74241c0c4fc8e8166f1a79a05d5b0dd95813a74b094529f317d5c39d235"),
            serialized);
    }

    @Test
    public void testOfferSerialization() {
        Offer offer = new Offer(List.of(Bytes.fromHexString("0x010203")));
        Bytes serialized = offer.serialize();
        assertEquals(Bytes.fromHexString("0x060400000004000000010203"), serialized);
    }

    @Test
    public void testAcceptSerialization() {
        Accept accept = new Accept(Bytes.fromHexString("0x0102").toInt(), Bytes.fromHexString("0x01"));
        Bytes serialized = accept.serialize();
        assertEquals(Bytes.fromHexString("0x070102060000000101"), serialized);
    }

}
