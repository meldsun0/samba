package samba.domain.dht;

import org.apache.tuweni.bytes.Bytes;
import samba.domain.node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestHelper {


    private static final AtomicInteger nextAvailablePort = new AtomicInteger(1);

    private static final String IP_ADDR = "127.0.0.1";


    public static List<BucketEntry> createBucketEntryList(final int count)  {
        return Stream.generate(TestHelper::createBucketEntry).limit(count).collect(Collectors.toList());
    }

    public static BucketEntry createBucketEntry()  {
        final int port = nextAvailablePort.incrementAndGet();
        return new BucketEntry(new Node(Bytes.random(20), createIP(),port));
    }

    public static InetAddress createIP(){
        final InetAddress ip;
        try {
            ip = InetAddress.getByName(IP_ADDR);
        } catch (final UnknownHostException e) {
                throw new IllegalArgumentException("Invalid ip address or hostname.");
        }
        return ip;
    }

}
