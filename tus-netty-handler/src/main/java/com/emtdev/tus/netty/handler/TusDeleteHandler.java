package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.extension.TerminationExtension;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

@ChannelHandler.Sharable
public class TusDeleteHandler extends TusBaseRequestHandler {


    public TusDeleteHandler(TusConfiguration tusConfiguration) {
        super(tusConfiguration);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        HttpResponse response;

        if (ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.TERMINATION)) {

            String fileId = resolveFileFromUri(msg.uri());

            TerminationExtension terminationExtension = (TerminationExtension) getConfiguration().getStore();

            try {
                terminationExtension.delete(fileId);
                response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.NO_CONTENT);
            } catch (Exception e) {
                response =
                        HttpResponseUtils.createHttpResponse(HttpResponseStatus.NOT_FOUND);
            }

        } else {
            response =
                    HttpResponseUtils.createHttpResponse(HttpResponseStatus.GONE);
        }

        ctx.writeAndFlush(response).
                addListener(ChannelFutureListener.CLOSE);
    }

}

