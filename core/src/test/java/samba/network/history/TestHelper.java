package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.util.Functions;

import java.util.Random;

public class TestHelper {

    public static NodeRecord createNodeRecord(){
        return  new NodeRecordBuilder()
                .secretKey(Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey())
                .address("12.34.45.67", Integer.parseInt("9001"))
                .build();
    }
}
