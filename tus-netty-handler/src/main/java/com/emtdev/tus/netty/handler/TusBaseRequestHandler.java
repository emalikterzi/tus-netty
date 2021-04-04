package com.emtdev.tus.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.internal.StringUtil;

@ChannelHandler.Sharable
public abstract class TusBaseRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final TusConfiguration configuration;

    public TusBaseRequestHandler(TusConfiguration tusConfiguration) {
        this.configuration = tusConfiguration;
    }

    public TusConfiguration getConfiguration() {
        return configuration;
    }


    public static String resolveFileFromUri(String uri) {
        if (StringUtil.isNullOrEmpty(uri)) {
            return null;
        }
        String[] uriParts = uri.split("/");
        return uriParts[uriParts.length - 1];
    }

}
