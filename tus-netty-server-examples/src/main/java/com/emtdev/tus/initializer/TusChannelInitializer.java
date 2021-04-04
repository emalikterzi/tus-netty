package com.emtdev.tus.initializer;


import com.emtdev.tus.core.TusLocationProvider;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.custom.DefaultFileIdGenerator;
import com.emtdev.tus.custom.DefaultLocationProvider;
import com.emtdev.tus.custom.FileDiskConfigStore;
import com.emtdev.tus.netty.handler.TusConfiguration;
import com.emtdev.tus.netty.handler.TusNettyDecoder;
import com.emtdev.tus.store.FileStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class TusChannelInitializer extends ChannelInitializer<Channel> {
    private final TusConfiguration tusConfiguration;

    public TusChannelInitializer() {
        TusStore store = new FileStore("/Users/emalikterzi/tus-sample", new FileDiskConfigStore("/Users/emalikterzi/tus-sample/meta"));
        TusLocationProvider locationProvider = new DefaultLocationProvider();
        tusConfiguration = new TusConfiguration("/files/", store, locationProvider, new DefaultFileIdGenerator(), 0);
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        addChannels(p);
    }

    final void addChannels(ChannelPipeline p) {
        TusNettyDecoder tusNettyDecoder = new TusNettyDecoder(tusConfiguration);


        p.addLast("handler1", new HttpResponseEncoder());
        p.addLast("handler2", new HttpRequestDecoder());


        p.addLast(tusNettyDecoder);

    }

}
