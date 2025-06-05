package samba;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.IdentitySchema;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.ethereum.beacon.discovery.util.Functions;

public class TestHelper {

  public static NodeRecord createNodeRecord() {
    return new NodeRecordBuilder()
        .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
        .build();
  }

  public static NodeRecord createNodeAtDistance(final Bytes sourceNode, final int distance) {
    final BitSet bits = BitSet.valueOf(sourceNode.reverse().toArray());
    bits.flip(distance - 1);
    final byte[] targetNodeId = new byte[sourceNode.size()];
    final byte[] src = bits.toByteArray();
    System.arraycopy(src, 0, targetNodeId, 0, src.length);
    final Bytes nodeId = Bytes.wrap(targetNodeId).reverse();
    return SimpleIdentitySchemaInterpreter.createNodeRecord(
        nodeId, new InetSocketAddress("127.0.0.1", 2));
  }

  public static NodeRecord createNodeRecord(
      final Bytes nodeId, final InetSocketAddress udpAddress) {
    return createNodeRecord(
        nodeId,
        new EnrField(EnrField.IP_V4, Bytes.wrap(udpAddress.getAddress().getAddress())),
        new EnrField(EnrField.UDP, udpAddress.getPort()));
  }

  public static NodeRecord createNodeRecord(final Bytes nodeId, final EnrField... extraFields) {
    final List<EnrField> fields = new ArrayList<>(List.of(extraFields));
    fields.add(new EnrField(EnrField.ID, IdentitySchema.V4));
    fields.add(new EnrField(EnrField.PKEY_SECP256K1, nodeId));
    return new NodeRecordFactory(new SimpleIdentitySchemaInterpreter())
        .createFromValues(UInt64.ONE, fields);
  }
}
