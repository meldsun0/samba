package samba.domain.messages.extensions.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.junit.jupiter.api.Test;

public class ClientInfoAndCapabilitiesTest {

  @Test
  public void testSszDecode() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        ClientInfoAndCapabilities.fromSszBytes(
            Bytes.fromHexString(
                "0x28000000010000000000000000000000000000000000000000000000000000000000000032000000636c69656e74496e666f010002000300"));
    assertEquals("clientInfo", clientInfoAndCapabilities.getClientInfo());
    assertEquals(UInt256.valueOf(1), clientInfoAndCapabilities.getDataRadius());
    assertEquals(
        List.of(UInt16.valueOf(1), UInt16.valueOf(2), UInt16.valueOf(3)),
        clientInfoAndCapabilities.getCapabilities());
  }

  @Test
  public void testSszEncode() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "clientInfo",
            UInt256.valueOf(1),
            List.of(UInt16.valueOf(1), UInt16.valueOf(2), UInt16.valueOf(3)));
    Bytes encodedBytes = clientInfoAndCapabilities.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString(
            "0x28000000010000000000000000000000000000000000000000000000000000000000000032000000636c69656e74496e666f010002000300"));
  }

  @Test
  public void testSszExtensionPingDecode() {
    Ping ping =
        Ping.fromSSZBytes(
            Bytes.fromHexString(
                "0x00010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff550000007472696e2f76302e312e312d62363166646335632f6c696e75782d7838365f36342f7275737463312e38312e3000000100ffff"));
    assertEquals(
        ExtensionType.CLIENT_INFO_AND_CAPABILITIES,
        ExtensionType.fromValue(ping.getPayloadType().getValue()));
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        ClientInfoAndCapabilities.fromSszBytes(ping.getPayload());
    assertEquals(
        "trin/v0.1.1-b61fdc5c/linux-x86_64/rustc1.81.0", clientInfoAndCapabilities.getClientInfo());
    assertEquals(
        UInt256.MAX_VALUE.subtract(UInt256.ONE), clientInfoAndCapabilities.getDataRadius());
    assertEquals(
        List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE),
        clientInfoAndCapabilities.getCapabilities());
  }

  @Test
  public void testSszExtensionPingDecodeNoClientInfo() {
    Ping ping =
        Ping.fromSSZBytes(
            Bytes.fromHexString(
                "0x00010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2800000000000100ffff"));
    assertEquals(
        ExtensionType.CLIENT_INFO_AND_CAPABILITIES,
        ExtensionType.fromValue(ping.getPayloadType().getValue()));
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        ClientInfoAndCapabilities.fromSszBytes(ping.getPayload());
    assertEquals("", clientInfoAndCapabilities.getClientInfo());
    assertEquals(
        UInt256.MAX_VALUE.subtract(UInt256.ONE), clientInfoAndCapabilities.getDataRadius());
    assertEquals(
        List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE),
        clientInfoAndCapabilities.getCapabilities());
  }

  @Test
  public void testSszExtensionPingEncode() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "trin/v0.1.1-b61fdc5c/linux-x86_64/rustc1.81.0",
            UInt256.MAX_VALUE.subtract(UInt256.ONE),
            List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE));
    Ping ping =
        new Ping(
            UInt64.ONE,
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            clientInfoAndCapabilities.getSszBytes());
    Bytes encodedBytes = ping.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString(
            "0x00010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff550000007472696e2f76302e312e312d62363166646335632f6c696e75782d7838365f36342f7275737463312e38312e3000000100ffff"));
  }

  @Test
  public void testSszExtensionPingEncodeNoClientInfo() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "",
            UInt256.MAX_VALUE.subtract(UInt256.ONE),
            List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE));
    Ping ping =
        new Ping(
            UInt64.ONE,
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            clientInfoAndCapabilities.getSszBytes());
    Bytes encodedBytes = ping.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString(
            "0x00010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2800000000000100ffff"));
  }

  @Test
  public void testSszExtensionPongDecode() {
    Pong pong =
        Pong.fromSSZBytes(
            Bytes.fromHexString(
                "0x01010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff550000007472696e2f76302e312e312d62363166646335632f6c696e75782d7838365f36342f7275737463312e38312e3000000100ffff"));
    assertEquals(
        ExtensionType.CLIENT_INFO_AND_CAPABILITIES,
        ExtensionType.fromValue(pong.getPayloadType().getValue()));
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        ClientInfoAndCapabilities.fromSszBytes(pong.getPayload());
    assertEquals(
        "trin/v0.1.1-b61fdc5c/linux-x86_64/rustc1.81.0", clientInfoAndCapabilities.getClientInfo());
    assertEquals(
        UInt256.MAX_VALUE.subtract(UInt256.ONE), clientInfoAndCapabilities.getDataRadius());
    assertEquals(
        List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE),
        clientInfoAndCapabilities.getCapabilities());
  }

  @Test
  public void testSszExtensionPongDecodeNoClientInfo() {
    Pong pong =
        Pong.fromSSZBytes(
            Bytes.fromHexString(
                "0x01010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2800000000000100ffff"));
    assertEquals(
        ExtensionType.CLIENT_INFO_AND_CAPABILITIES,
        ExtensionType.fromValue(pong.getPayloadType().getValue()));
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        ClientInfoAndCapabilities.fromSszBytes(pong.getPayload());
    assertEquals("", clientInfoAndCapabilities.getClientInfo());
    assertEquals(
        UInt256.MAX_VALUE.subtract(UInt256.ONE), clientInfoAndCapabilities.getDataRadius());
    assertEquals(
        List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE),
        clientInfoAndCapabilities.getCapabilities());
  }

  @Test
  public void testSszExtensionPongEncode() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "trin/v0.1.1-b61fdc5c/linux-x86_64/rustc1.81.0",
            UInt256.MAX_VALUE.subtract(UInt256.ONE),
            List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE));
    Pong pong =
        new Pong(
            UInt64.ONE,
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            clientInfoAndCapabilities.getSszBytes());
    Bytes encodedBytes = pong.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString(
            "0x01010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff550000007472696e2f76302e312e312d62363166646335632f6c696e75782d7838365f36342f7275737463312e38312e3000000100ffff"));
  }

  @Test
  public void testSszExtensionPongEncodeNoClientInfo() {
    ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "",
            UInt256.MAX_VALUE.subtract(UInt256.ONE),
            List.of(UInt16.ZERO, UInt16.valueOf(1), UInt16.MAX_VALUE));
    Pong pong =
        new Pong(
            UInt64.ONE,
            ExtensionType.CLIENT_INFO_AND_CAPABILITIES.getExtensionCode(),
            clientInfoAndCapabilities.getSszBytes());
    Bytes encodedBytes = pong.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString(
            "0x01010000000000000000000e00000028000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2800000000000100ffff"));
  }
}
