package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.extension.ChecksumExtension;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@ChannelHandler.Sharable
public class TusOptionsHandler extends TusBaseRequestHandler {

    public TusOptionsHandler(TusConfiguration tusConfiguration) {
        super(tusConfiguration);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        /**
         * An OPTIONS request MAY be used to gather information about the Server’s current configuration.
         * A successful response indicated by the 204 No Content or 200 OK status MUST contain the Tus-Version header. It MAY include the Tus-Extension and Tus-Max-Size headers.
         *
         * The Client SHOULD NOT include the Tus-Resumable header in the request and the Server MUST ignore the header.
         */
        String extensions = ExtensionUtils.extensionHeaderValue(getConfiguration().getStore());

        HttpResponse fullHttpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
        HttpHeaders httpHeaders = fullHttpResponse.headers();

        if (!extensions.equals("")) {
            httpHeaders.add(HttpRequestAccessor.TUS_EXTENSION, extensions);
        }

        if (ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CHECKSUM)) {
            ChecksumExtension checksumExtension = (ChecksumExtension) getConfiguration().getStore();
            String value = ExtensionUtils.checksumHeaderValue(checksumExtension);
            httpHeaders.add(HttpRequestAccessor.TUS_CHECKSUM_ALG, value);
        }

        if (getConfiguration().getMaxFileSize() != 0) {
            httpHeaders.add(HttpRequestAccessor.TUS_MAX_SIZE, getConfiguration().getMaxFileSize());
        }

        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);

    }
}
