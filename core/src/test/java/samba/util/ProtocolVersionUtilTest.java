package samba.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.TestHelper;

import java.util.List;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProtocolVersionUtilTest {

  private NodeRecord testRecord;

  @BeforeEach
  public void setUp() {
    this.testRecord = TestHelper.createNodeRecord();
  }

  @Test
  public void testSetAndGetSupportedProtocolVersions() {
    ProtocolVersionUtil.setSupportedProtocolVersions(testRecord);
    assertEquals(
        ProtocolVersionUtil.getSupportedProtocolVersions(testRecord),
        ProtocolVersionUtil.SUPPORTED_PROTOCOL_VERSIONS);
  }

  @Test
  public void testSetAndGetSupportedProtocolVersionsWithCustomList() {
    ProtocolVersionUtil.setSupportedProtocolVersions(testRecord, List.of(1, 2, 3));
    assertEquals(ProtocolVersionUtil.getSupportedProtocolVersions(testRecord), List.of(1, 2, 3));
  }
}
