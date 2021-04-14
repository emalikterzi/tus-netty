package com.emtdev.tus.spring.server;


import com.emtdev.tus.netty.handler.TusConfiguration;
import com.emtdev.tus.netty.handler.TusNettyDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class TusChannelInitializer extends ChannelInitializer<Channel> {

    private final TusConfiguration tusConfiguration;

    public TusChannelInitializer(TusConfiguration tusConfiguration) {
        this.tusConfiguration = tusConfiguration;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        addChannels(p);
    }

    final void addChannels(ChannelPipeline p) {
        TusNettyDecoder tusNettyDecoder = new TusNettyDecoder(tusConfiguration);
        p.addLast("httpResponseEncoder", new HttpResponseEncoder());
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());
        p.addLast(tusNettyDecoder);
    }

}
