package samba.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class NettyServer {

    private final EventLoopGroup boss = new NioEventLoopGroup(1);
    private final EventLoopGroup workers = new NioEventLoopGroup(10);
    private ChannelFuture server;

    private String host;
    private int port;

    public NettyServer(String host, int port){
        this.host = host;
        this.port = port;
    }



    public CompletableFuture<InetSocketAddress> start() {
        final CompletableFuture<InetSocketAddress> listeningPortFuture = new CompletableFuture<>();
        this.server = new ServerBootstrap()
                        .group(boss, workers)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel socketChannel) throws Exception {

                                //TODO notify possible subscribers
                                //TODO add a TimeOutHandler | SSL | TLS
//                                socketChannel.pipeline().addLast(
//                                        new HandshakeHandlerInbound(
//                                                nodeKey,
//                                                config.getSupportedProtocols(),
//                                                localNode,
//                                                connectionFuture,
//                                                eventDispatcher,
//                                                metricsSystem,
//                                                this,
//                                                this,
//                                                peerTable));

                            }
                        })
                        .bind(this.host, this.port);

        server.addListener(
                future -> {
                    final InetSocketAddress socketAddress = (InetSocketAddress) server.channel().localAddress();
                    if (!future.isSuccess() || socketAddress == null) {
                        final String message = String.format( "Unable to start listening on %s:%s. Check for port conflicts.", this.host, this.port);
                        listeningPortFuture.completeExceptionally(new IllegalStateException(message, future.cause()));
                        return;
                    }

                    listeningPortFuture.complete(socketAddress);
                });

        return listeningPortFuture;
    }


}
