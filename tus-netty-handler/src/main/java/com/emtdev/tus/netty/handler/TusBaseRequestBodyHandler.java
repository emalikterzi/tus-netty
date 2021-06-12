package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.ExpirationExtension;
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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class TusBaseRequestBodyHandler extends ChannelInboundHandlerAdapter {


    protected final String httpMethod;
    private final TusConfiguration configuration;
    protected State currentState = State.NEW;
    protected String fileId;

    public TusBaseRequestBodyHandler(TusConfiguration configuration, String httpMethpd) {
        this.configuration = configuration;
        this.httpMethod = httpMethpd;
    }

    public static String resolveFileFromUri(String uri) {
        if (StringUtil.isNullOrEmpty(uri)) {
            return null;
        }
        String[] uriParts = uri.split("/");
        return uriParts[uriParts.length - 1];
    }

    public TusConfiguration getConfiguration() {
        return configuration;
    }

    protected abstract void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception;

    protected abstract void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception;

    protected abstract void onWriteFinished(ChannelHandlerContext ctx, HttpContent msg);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (this.currentState == State.BODY && this.fileId != null) {
            this.getConfiguration().getStore().finalizeFile(fileId);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
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
        _writeInternal(ctx, msg, requestFinished);
    }

    public void _writePatch(ChannelHandlerContext ctx, HttpContent msg) {
        boolean requestFinished = (msg instanceof LastHttpContent);
        _writeInternal(ctx, msg, requestFinished);
    }

    private void _writeInternal(ChannelHandlerContext ctx, HttpContent msg, boolean requestFinished) {
        TusStore tusStore = getConfiguration().getStore();
        OperationResult operationResult = tusStore.write(fileId, msg.content());

        if (!operationResult.isSuccess()) {
            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Operation Result Failed On Last Byte");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        if (requestFinished) {
            tusStore.finalizeFile(fileId);
            onWriteFinished(ctx, msg);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        boolean httpRequest = msg instanceof HttpRequest;
        boolean httpContent = msg instanceof HttpContent;

        if (httpRequest || httpContent) {

            if (httpRequest) {
                HttpRequest message = (HttpRequest) msg;
                currentState = State.REQUEST;
                this.channelRead0(ctx, message);
            } else {
                HttpContent message = (HttpContent) msg;
                currentState = State.BODY;
                this.channelRead0(ctx, message);
            }

            ReferenceCountUtil.release(msg);

        } else {
            ctx.fireChannelRead(msg);
        }
    }


    public enum State {
        NEW, REQUEST, BODY
    }
}
