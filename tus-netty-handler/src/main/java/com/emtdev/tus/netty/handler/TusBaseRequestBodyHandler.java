package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.CreationExtension;
import com.emtdev.tus.core.extension.CreationWithUploadExtension;
import com.emtdev.tus.core.extension.ExpirationExtension;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

public abstract class TusBaseRequestBodyHandler extends ChannelInboundHandlerAdapter {

    protected String fileId;

    private final ReentrantLock lock = new ReentrantLock();

    private final TusConfiguration configuration;

    public TusBaseRequestBodyHandler(TusConfiguration tusConfiguration) {
        this.configuration = tusConfiguration;
    }

    public TusConfiguration getConfiguration() {
        return configuration;
    }

    protected abstract void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception;

    protected abstract void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception;

    protected abstract void onWriteFinished(ChannelHandlerContext ctx, HttpContent msg);

    public static String resolveFileFromUri(String uri) {
        if (StringUtil.isNullOrEmpty(uri)) {
            return null;
        }
        String[] uriParts = uri.split("/");
        return uriParts[uriParts.length - 1];
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            getConfiguration().getStore().afterConnectionClose(fileId);
        } catch (Exception e) {

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            getConfiguration().getStore().onException(fileId, cause);
        } catch (Exception e) {

        }
    }

    private String getDateStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    protected void addExpireHeaderToResponse(HttpResponse response) {
        if (ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.EXPIRATION)) {

            ExpirationExtension expirationExtension = (ExpirationExtension) getConfiguration().getStore();
            Date expirationDate = expirationExtension.expires(fileId);

            if (expirationDate != null) {
                String dateStr = getDateStr(expirationDate);
                response.headers().add(HttpRequestAccessor.UPLOAD_EXPIRES, dateStr);
            }
        }
    }


    public void _writePost(ChannelHandlerContext ctx, HttpContent msg) {
        boolean requestFinished = (msg instanceof LastHttpContent);

        ByteBuf body = msg.content();

        CreationWithUploadExtension creationExtension = (CreationWithUploadExtension) getConfiguration().getStore();

        OperationResult operationResult = creationExtension.createAndWrite(fileId, body, requestFinished);

        _writeInternal(ctx, msg, operationResult, requestFinished);
    }

    public void _writePatch(ChannelHandlerContext ctx, HttpContent msg) {
        boolean requestFinished = (msg instanceof LastHttpContent);

        ByteBuf body = msg.content();

        CreationExtension creationExtension = getConfiguration().getStore();

        OperationResult operationResult = creationExtension.write(fileId, body, requestFinished);

        _writeInternal(ctx, msg, operationResult, requestFinished);
    }


    private void _writeInternal(ChannelHandlerContext ctx, HttpContent msg, OperationResult operationResult, boolean requestFinished) {

        ReferenceCountUtil.release(msg);

        if (!operationResult.isSuccess()) {

            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, operationResult.getFailedReason());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            return;
        }

        if (requestFinished) {
            onWriteFinished(ctx, msg);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        boolean httpRequest = msg instanceof HttpRequest;
        boolean httpContent = msg instanceof HttpContent;

        if (httpRequest || httpContent) {

            lock.lock();

            try {
                if (httpRequest) {
                    HttpRequest message = (HttpRequest) msg;
                    this.channelRead0(ctx, message);
                } else {
                    HttpContent message = (HttpContent) msg;
                    this.channelRead0(ctx, message);
                }
            } finally {
                lock.unlock();
            }

        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
