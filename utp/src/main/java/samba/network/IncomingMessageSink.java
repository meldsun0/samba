package samba.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.FluxSink;
import samba.pipeline.Envelope;

public class IncomingMessageSink extends SimpleChannelInboundHandler<Envelope> {
    private static final Logger LOG = LogManager.getLogger(IncomingMessageSink.class);
    private final FluxSink<Envelope> messageSink;

    public IncomingMessageSink(FluxSink<Envelope> messageSink) {
        this.messageSink = messageSink;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Envelope msg) {
        LOG.trace(() -> String.format("Incoming packet %s in session %s", msg, ctx));
        messageSink.next(msg);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        LOG.error("Unexpected exception caught", cause);
    }
}
