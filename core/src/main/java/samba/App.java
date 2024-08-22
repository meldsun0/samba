/*
 * SPDX-License-Identifier: Apache-2.0
 */
package samba;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.ethereum.beacon.discovery.util.Functions;

public class App {

    public static void main(String[] args) throws Exception {

        InetAddress address = InetAddress.getByName("23.44.56.78");
        int port = Integer.parseInt("9001");
        int pkSeed = new Random().nextInt();

        List<NodeRecord> bootnodes = new ArrayList<>();
        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64("-Ku4QImhMc1z8yCiNJ1TyUxdcfNucje3BGwEHzodEZUan8PherEo4sF7pPHPSIB1NNuSg5fZy7qFsjmUKs2ea1Whi0EBh2F0dG5ldHOIAAAAAAAAAACEZXRoMpD1pf1CAAAAAP__________gmlkgnY0gmlwhBLf22SJc2VjcDI1NmsxoQOVphkDqal4QzPMksc5wnpuC3gvSC8AfbFOnZY_On34wIN1ZHCCIyg"));

        System.out.println("Starting discovery...");
        final KeyPair keyPair = Functions.randomKeyPair(new Random(pkSeed));

        final NodeRecord nodeRecord =
                new NodeRecordBuilder()
                        .secretKey(keyPair.secretKey())
                        .address(address.getHostAddress(), port)
                        .build();

        DiscoverySystemBuilder discoverySystemBuilder =
                new DiscoverySystemBuilder()
                        .listen("0.0.0.0", port)
                        .localNodeRecord(nodeRecord)
                        .secretKey(keyPair.secretKey())
                        .bootnodes(bootnodes);
        final DiscoverySystem discoverySystem = discoverySystemBuilder.build();
        discoverySystem.start().get(5, TimeUnit.SECONDS);

        //     NodeRecord myNode = discoverySystem.getLocalNodeRecord();
        System.out.println("Discovery started!");
        Set<NodeRecord> activeKnownNodes = new HashSet<>();
        while (true) {
            List<NodeRecord> newActiveNodes =
                    discoverySystem
                            .streamLiveNodes()
                            .filter(r -> !activeKnownNodes.contains(r))
                            .collect(Collectors.toList());

            activeKnownNodes.addAll(newActiveNodes);
            newActiveNodes.forEach(
                    n -> {
                        System.out.println(
                                "New active node: "
                                        + n.getNodeId()
                                        + " @ "
                                        + n.getUdpAddress().map(InetSocketAddress::toString).orElse("<unknown>"));
                    });
            Thread.sleep(500);
        }
    }
}