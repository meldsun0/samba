package samba.services.discovery;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import samba.config.DiscoveryConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5ServiceTest {

  private Discv5Service discv5Service;

  @BeforeEach
  public void setUp() {
    discv5Service =
        new Discv5Service(
            mock(MetricsSystem.class),
            null,
            DiscoveryConfig.builder().build(),
            createRandomSecretKey(),
            null);
  }

  @Test
  public void shouldUpdateEnrSocketWhenFieldsChange() throws Exception {
    InetSocketAddress socketAddress =
        new InetSocketAddress(InetAddress.getByName("192.168.1.100"), 30303);
    boolean result = discv5Service.updateEnrSocket(socketAddress, false);
    assertThat(result).isTrue();
    assertEquals(
        Bytes.wrap(socketAddress.getAddress().getAddress()),
        this.discv5Service.getHomeNodeRecord().get(EnrField.IP_V4));
    assertEquals(
        Bytes.ofUnsignedInt(socketAddress.getPort()),
        this.discv5Service.getHomeNodeRecord().get(EnrField.UDP));
    result = discv5Service.updateEnrSocket(socketAddress, true);
    assertThat(result).isTrue();
    assertEquals(
        Bytes.wrap(socketAddress.getAddress().getAddress()),
        this.discv5Service.getHomeNodeRecord().get(EnrField.IP_V4));
    assertEquals(
        Bytes.ofUnsignedInt(socketAddress.getPort()),
        this.discv5Service.getHomeNodeRecord().get(EnrField.TCP));
  }

  private SECP256K1.SecretKey createRandomSecretKey() {
    final SECP256K1.KeyPair randomKey = Functions.randomKeyPair(new Random(new Random().nextInt()));
    return randomKey.secretKey();
  }
}
