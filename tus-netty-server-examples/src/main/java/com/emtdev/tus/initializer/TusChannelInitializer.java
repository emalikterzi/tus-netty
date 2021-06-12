package com.emtdev.tus.initializer;


import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.emtdev.tus.core.TusLocationProvider;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.impl.InMemoryTusStoreConfig;
import com.emtdev.tus.custom.DefaultFileIdGenerator;
import com.emtdev.tus.custom.DefaultLocationProvider;
import com.emtdev.tus.netty.handler.TusConfiguration;
import com.emtdev.tus.netty.handler.TusNettyDecoder;
import com.emtdev.tus.store.S3Store;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class TusChannelInitializer extends ChannelInitializer<Channel> {
    private final static String baseDirectory = "/Users/emalikterzi/tus-sample";
    private final TusConfiguration tusConfiguration;

    public TusChannelInitializer() {

        AmazonS3ClientBuilder builder
                = AmazonS3Client.builder();
        builder
                .setCredentials(new EnvironmentVariableCredentialsProvider());
        builder.setRegion("eu-west-1");
        AmazonS3 amazonS3 = builder.build();

        TusStore tusStore = new S3Store(baseDirectory, new InMemoryTusStoreConfig(), amazonS3, "test-bucket-tus");
        TusLocationProvider locationProvider = new DefaultLocationProvider();
        tusConfiguration = new TusConfiguration("/files/", tusStore, locationProvider, new DefaultFileIdGenerator(), 0);
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
//        p.addLast("handler3", new HttpObjectAggregator(64 * 64 * 1024));


        p.addLast(tusNettyDecoder);

    }

}
