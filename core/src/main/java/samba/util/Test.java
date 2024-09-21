package samba.util;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.TalkHandler;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.ethereum.beacon.discovery.util.Functions;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Test {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String arg[]) throws InterruptedException, ExecutionException, TimeoutException {
          //  main2();
    }

    private static void  createLocalNodeRecordListener(NodeRecord nodeRecord, NodeRecord nodeRecord1) {
        LOG.info("Talk Hanlder hetr");
    }

    private static NodeRecord createNodeRecord(final SECP256K1.KeyPair keyPair, final String ip, final int port) {
        return new NodeRecordBuilder()
                .secretKey(keyPair.secretKey())
                .address(ip, port)
                .build();
    }



    static void printHelp() {
        System.out.println("DiscoveryTestServer arguments:");
        System.out.println("<externalIp> <listenPort> [privateKeySeed] [bootNode1] [bootNode2] ...");
        System.out.println("Examples:");
        System.out.println("23.44.56.78 9000");
        System.out.println("23.44.56.78 9000 5");
        System.out.println(
                "23.44.56.78 9001 123 -IS4QIrMgVOYuw2mq68f9hFGTlPzJT5pRWIqKTYL93C5xasmfUGUydi2XrjsbxO1MLYGEl1rR5H1iov6gxOyhegW9hYBgmlkgnY0gmlwhLyGRgGJc2VjcDI1NmsxoQPKY0yuDUmstAHYpMa2_oxVtw0RW_QAdpzBQA8yWM0xOIN1ZHCCIyo");
    }

//
//    public static void main2() throws InterruptedException, ExecutionException, TimeoutException {
//
//
//        List bootnodes = new ArrayList<>();
//
//        //# Trin bootstrap nodes
//        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64("-Jy4QIs2pCyiKna9YWnAF0zgf7bT0GzlAGoF8MEKFJOExmtofBIqzm71zDvmzRiiLkxaEJcs_Amr7XIhLI74k1rtlXICY5Z0IDAuMS4xLWFscGhhLjEtMTEwZjUwgmlkgnY0gmlwhKEjVaWJc2VjcDI1NmsxoQLSC_nhF1iRwsCw0n3J4jRjqoaRxtKgsEe5a-Dz7y0JloN1ZHCCIyg"));
//        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64("-Jy4QH4_H4cW--ejWDl_W7ngXw2m31MM2GT8_1ZgECnfWxMzZTiZKvHDgkmwUS_l2aqHHU54Q7hcFSPz6VGzkUjOqkcCY5Z0IDAuMS4xLWFscGhhLjEtMTEwZjUwgmlkgnY0gmlwhJ31OTWJc2VjcDI1NmsxoQPC0eRkjRajDiETr_DRa5N5VJRm-ttCWDoO1QAMMCg5pIN1ZHCCIyg"));
//
//
//        System.out.println("Starting discovery...");
////        final Random rnd = new Random(new Random().nextInt());
////        final SECP256K1.KeyPair keyPair = Functions.randomKeyPair(rnd);
//
////        final NodeRecord nodeRecord =
////                new NodeRecordBuilder()
////                        .secretKey(keyPair.secretKey())
////                        .address("0.0.0.0", 9090)
////                        .build();
////
////        DiscoverySystemBuilder discoverySystemBuilder =
////                new DiscoverySystemBuilder()
////                        .listen("0.0.0.0", 9090) //
////                        .localNodeRecord(nodeRecord) //
////                        .secretKey(keyPair.secretKey()) //
////                        .bootnodes(bootnodes);
//
//
//
////        final DiscoverySystem discoverySystem = discoverySystemBuilder.build();
//        discoverySystem.start().get(5, TimeUnit.SECONDS);
//
//        //NodeRecord myNode = discoverySystem.getLocalNodeRecord();
//        Set<NodeRecord> activeKnownNodes = new HashSet<>();
//
//
//
//
//
//        while (true) {
//            List<NodeRecord> newActiveNodes =
//                    discoverySystem
//                            .streamLiveNodes()
//                            .filter(r -> !activeKnownNodes.contains(r))
//                            .collect(Collectors.toList());
//
//            activeKnownNodes.addAll(newActiveNodes);
//            newActiveNodes.forEach(
//                    n -> {
//                        System.out.println(
//                                "New active node: "
//                                        + n.getNodeId()
//                                        + " @ "
//                                        + n.getUdpAddress().map(InetSocketAddress::toString).orElse("<unknown>"));
//                        try {
//                            TimeUnit.SECONDS.sleep(5);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        System.out.println("************************");
//                        discoverySystem.talk(n,  Bytes.fromHexString("0x500B"), Bytes.fromHexString("0x0001000000000000000c000000feffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))
//                                .thenApply(fn -> {
//                                    System.out.println("Sent message to node: " + n.getNodeId());
//                                    LOG.info("Received response: " + fn.toHexString());
//                                    return fn;})
//                                .exceptionally(e -> {
//                                    LOG.error("Error sending message: " + e.getMessage());
//                                    return null;})
//                                .thenAccept(action -> {
//                                    LOG.info("Message sent successfully");
//                                });
//
//                    });
//
//            Thread.sleep(500);
//        }
//    }
}
