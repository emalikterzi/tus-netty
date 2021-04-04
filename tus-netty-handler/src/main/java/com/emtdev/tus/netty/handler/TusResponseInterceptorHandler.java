package com.emtdev.tus.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

@ChannelHandler.Sharable
public class TusResponseInterceptorHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;

            HttpHeaders httpHeaders = httpResponse.headers();
            httpHeaders.add("Access-Control-Allow-Origin", "*");
            httpHeaders.add("Access-Control-Allow-Methods", "POST, HEAD, PATCH, OPTIONS");
            httpHeaders.add(
                    "Access-Control-Allow-Headers",
                    "Authorization, Content-Type, Upload-Concat, Location, Tus-Extension, Tus-Max-Size, Tus-Resumable, Tus-Version, Upload-Defer-Length, Upload-Length, Upload-Metadata, Upload-Offset, X-HTTP-Method-Override, X-Requested-With");
            httpHeaders.add(
                    "Access-Control-Expose-Headers",
                    "Authorization, Content-Type, Upload-Concat, Location, Tus-Extension, Tus-Max-Size, Tus-Resumable, Tus-Version, Upload-Defer-Length, Upload-Length, Upload-Metadata, Upload-Offset, X-HTTP-Method-Override, X-Requested-With");

            httpHeaders.add("Access-Control-Max-Age", 86400);

            httpHeaders.add("Tus-Version", "1.0.0");
            httpHeaders.add("Tus-Resumable", "1.0.0");
        }

        ctx.write(msg, promise);
    }

}
