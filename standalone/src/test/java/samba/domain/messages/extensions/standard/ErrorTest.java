package samba.domain.messages.extensions.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.junit.jupiter.api.Test;

public class ErrorTest {

  @Test
  public void testSszDecode() {
    ErrorExtension error =
        ErrorExtension.fromSszBytes(Bytes.fromHexString("0x0000060000006572726f72"));
    assertEquals(UInt16.ZERO, error.getErrorCode());
    assertEquals("error", error.getMessage());
  }

  @Test
  public void testSszEncode() {
    ErrorExtension error = new ErrorExtension(UInt16.ZERO, "error");
    Bytes encodedBytes = error.getSszBytes();
    assertEquals(encodedBytes, Bytes.fromHexString("0x0000060000006572726f72"));
  }

  @Test
  public void testSszExtensionPongDecode() {
    Pong pong =
        Pong.fromSSZBytes(
            Bytes.fromHexString(
                "0x010100000000000000ffff0e00000002000600000068656c6c6f20776f726c64"));
    assertEquals(ExtensionType.ERROR, ExtensionType.fromValue(pong.getPayloadType().getValue()));
    ErrorExtension error = ErrorExtension.fromSszBytes(pong.getPayload());
    assertEquals(ErrorType.FAILED_TO_DECODE, ErrorType.fromCode(error.getErrorCode().getValue()));
    assertEquals("hello world", error.getMessage());
  }

  @Test
  public void testSszExtensionPongEncode() {
    ErrorExtension error =
        new ErrorExtension(ErrorType.FAILED_TO_DECODE.getErrorCode(), "hello world");
    Pong pong =
        new Pong(UInt64.valueOf(1), ExtensionType.ERROR.getExtensionCode(), error.getSszBytes());
    Bytes encodedBytes = pong.getSszBytes();
    assertEquals(
        encodedBytes,
        Bytes.fromHexString("0x010100000000000000ffff0e00000002000600000068656c6c6f20776f726c64"));
  }
}
