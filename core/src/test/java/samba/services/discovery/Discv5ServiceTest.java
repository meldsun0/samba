package samba.services.discovery;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samba.config.DiscoveryConfig;
import samba.domain.messages.IncomingRequestTalkHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Discv5ServiceTest {

    private Discv5Service discv5Service;

    @BeforeEach
    public void setUp() {
        discv5Service = new Discv5Service(mock(MetricsSystem.class), null, DiscoveryConfig.builder().build(), createRandomSecretKey(), null);
    }

    @Test
    public void shouldUpdateEnrSocketWhenFieldsChange() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("192.168.1.100"), 30303);
        boolean result = discv5Service.updateEnrSocket(socketAddress, false);
        assertThat(result).isTrue();
        assertEquals(Bytes.wrap(socketAddress.getAddress().getAddress()), this.discv5Service.getHomeNodeRecord().get(EnrField.IP_V4));
        assertEquals(Bytes.ofUnsignedInt(socketAddress.getPort()), this.discv5Service.getHomeNodeRecord().get(EnrField.UDP));
        result = discv5Service.updateEnrSocket(socketAddress, true);
        assertThat(result).isTrue();
        assertEquals(Bytes.wrap(socketAddress.getAddress().getAddress()), this.discv5Service.getHomeNodeRecord().get(EnrField.IP_V4));
        assertEquals(Bytes.ofUnsignedInt(socketAddress.getPort()), this.discv5Service.getHomeNodeRecord().get(EnrField.TCP));
    }

    private SECP256K1.SecretKey createRandomSecretKey() {
        final SECP256K1.KeyPair randomKey =
                Functions.randomKeyPair(new Random(new Random().nextInt()));
        return randomKey.secretKey();
    }
}
