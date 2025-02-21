package samba.utp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UTPManagerTests {

  private UTPManager utpManager;

  @BeforeEach
  public void before() {
    this.utpManager = new UTPManager(mock(Discv5Client.class));
  }

  @Test
  public void testParseAcceptedContents() {
    List<Bytes> content = this.utpManager.parseAcceptedContents(Bytes.fromHexString("0x00"));
    assertNotNull(content);
    assertTrue(!content.isEmpty());
    assertEquals(content.getFirst().toHexString(), "0x00");
  }
}
