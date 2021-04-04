package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.domain.FileStat;
import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import com.emtdev.tus.core.extension.CreationExtension;
import com.emtdev.tus.core.extension.ExpirationExtension;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;

import java.util.Date;

public class TusPatchHandler extends TusBaseRequestBodyHandler {


    public TusPatchHandler(TusConfiguration tusConfiguration) {
        super(tusConfiguration);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
        _writePatch(ctx, msg);
    }

    @Override
    protected void onWriteFinished(ChannelHandlerContext ctx, HttpContent msg) {
        CreationExtension creationExtension = getConfiguration().getStore();

        HttpResponse response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.NO_CONTENT);
        response.headers().add(HttpRequestAccessor.UPLOAD_OFFSET, creationExtension.offset(fileId));

        addExpireHeaderToResponse(response);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        HttpRequestAccessor accessor = HttpRequestAccessor.of(msg);

        if (!accessor.isContentTypeOffsetStream()) {
            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type was not application/offset+octet-stream");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        this.fileId = TusBaseRequestHandler.resolveFileFromUri(msg.uri());

        long uploadLength;

        if (ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CREATION_DEFER_LENGTH)) {

            CreationDeferLengthExtension creationDeferLengthExtension = (CreationDeferLengthExtension) getConfiguration().getStore();
            FileStat fileStat = creationDeferLengthExtension.configStore().get(this.fileId);

            if (!StringUtil.isNullOrEmpty(fileStat.getUploadDefer()) && fileStat.getUploadDefer().equals("1") && accessor.uploadLength() != 0) {
                fileStat.setUploadLength(accessor.uploadLength());
                creationDeferLengthExtension.configStore().update(fileStat);
            }

            uploadLength = fileStat.getUploadLength();
        } else {
            uploadLength = accessor.uploadLength();
        }

        CreationExtension creation = getConfiguration().getStore();

        boolean exist = creation.exist(fileId);

        if (!exist) {
            HttpResponse response =
                    HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.NOT_FOUND, "PATCH request against a non-existent resource");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        long contentLength = accessor.getContentLength();
        long fileOffset = creation.offset(fileId);

        if (uploadLength != (contentLength + fileOffset)) {
            HttpResponse response =
                    HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.CONFLICT, "File Offset with Upload Offset Conflict");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            _writePatch(ctx, fullHttpRequest);
        }
    }
}