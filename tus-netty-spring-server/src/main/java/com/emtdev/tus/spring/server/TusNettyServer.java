package com.emtdev.tus.spring.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


public class TusNettyServer {

    private final static Logger logger = LoggerFactory.getLogger(TusNettyServer.class);

    private TusSpringProperties tusSpringConfiguration;

    private TusChannelInitializer pipelineFactory;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public TusNettyServer(TusSpringProperties tusSpringConfiguration, TusChannelInitializer pipelineFactory) {
        this.tusSpringConfiguration = tusSpringConfiguration;
        this.pipelineFactory = pipelineFactory;
    }

    public void start() {
        this.startAsync().syncUninterruptibly();
    }

    public Future<Void> startAsync() {

        this.initGroups();

        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;

        if (this.tusSpringConfiguration.isUseLinuxNativeEpoll()) {
            channelClass = EpollServerSocketChannel.class;
        }

        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup).channel(channelClass).childHandler(this.pipelineFactory);

        this.applyConnectionOptions(b);

        InetSocketAddress addr = new InetSocketAddress(this.tusSpringConfiguration.getPort());

        if (this.tusSpringConfiguration.getHostname() != null) {
            addr = new InetSocketAddress(this.tusSpringConfiguration.getHostname(), this.tusSpringConfiguration.getPort());
        }

        return b.bind(addr).addListener(new FutureListener<Void>() {
            public void operationComplete(io.netty.util.concurrent.Future<Void> future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("TUS server started at port: {}", tusSpringConfiguration.getPort());
                } else {
                    logger.error("TUS server start failed at port: {}!", tusSpringConfiguration.getPort());
                }

            }
        });
    }


    protected void applyConnectionOptions(ServerBootstrap bootstrap) {
        TusSpringProperties.NettySocketConfig config = this.tusSpringConfiguration.getSocket();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
        if (config.getTcpSendBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getTcpSendBufferSize());
        }

        if (config.getTcpReceiveBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getTcpReceiveBufferSize());
            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(config.getTcpReceiveBufferSize()));
        }

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.isTcpKeepAlive());
        bootstrap.childOption(ChannelOption.SO_LINGER, config.getSoLinger());
        bootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
        bootstrap.option(ChannelOption.SO_BACKLOG, config.getAcceptBackLog());
    }

    protected void initGroups() {
        if (this.tusSpringConfiguration.isUseLinuxNativeEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(this.tusSpringConfiguration.getBossThreads());
            this.workerGroup = new EpollEventLoopGroup(this.tusSpringConfiguration.getWorkerThreads());
        } else {
            this.bossGroup = new NioEventLoopGroup(this.tusSpringConfiguration.getBossThreads());
            this.workerGroup = new NioEventLoopGroup(this.tusSpringConfiguration.getWorkerThreads());
        }

    }


    public void stop() {
        this.bossGroup.shutdownGracefully().syncUninterruptibly();
        this.workerGroup.shutdownGracefully().syncUninterruptibly();
    }
}
