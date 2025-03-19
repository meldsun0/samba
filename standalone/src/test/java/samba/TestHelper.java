package samba;

import java.util.Random;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;

public class TestHelper {

  public static NodeRecord createNodeRecord() {
    return new NodeRecordBuilder()
        .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
        .build();
  }
}
