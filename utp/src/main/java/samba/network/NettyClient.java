package samba.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.util.Map;

public class NettyClient implements UTPClient {

    private static final Logger LOG = LogManager.getLogger(NettyClient.class);
    private final Map<InternetProtocolFamily, NioDatagramChannel> channels;


    public NettyClient(final Publisher<NetworkParcel> outgoingStream, final Map<InternetProtocolFamily, NioDatagramChannel> channels) {
        this.channels = channels;
        Flux.from(outgoingStream)
                .subscribe(
                        networkPacket ->
                                send(networkPacket.getPacket().getBytes(), networkPacket.getDestination()));
        LOG.info("UDP discovery client started");
    }

    @Override
    public void stop() {

    }

    @Override
    public void send(Bytes data, InetSocketAddress destination) {
        final DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(data.toArray()), destination);
        final NioDatagramChannel channel = channels.get(InternetProtocolFamily.of(destination.getAddress()));
        if (channel == null) {
            LOG.trace(() -> String.format("Dropping packet %s because of IP version incompatibility", packet));
            return;
        }
        LOG.trace(() -> String.format("Sending packet %s", packet));
        channel.write(packet);
        channel.flush();
    }
}
