package com.emtdev.tus.netty.handler;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TusBaseRequestBodyHandler extends ChannelInboundHandlerAdapter {


    protected final String httpMethod;
    private final TusConfiguration configuration;
    protected State currentState = State.NEW;
    private boolean finalByteRead = false;
    protected String fileId;

    private TusByteBuffHandler tusByteBuffHandler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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

    protected abstract void onWriteFinished(ChannelHandlerContext ctx);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanWithoutExpect();
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cleanWithoutExpect();
        ctx.fireExceptionCaught(cause);
    }

    private void cleanWithoutExpect() {
        /**
         * son byte alinmadan baglanti kapatilirsa , byteBuff hemen kapatilmali.
         * bu operasyonun multi instancedaki senaryolari dusunulmeli.
         */
        if (this.currentState == State.BODY && !finalByteRead && this.tusByteBuffHandler != null) {
            this.tusByteBuffHandler.abort();
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
        _writeInternal(ctx, msg, requestFinished);
    }

    public void _writePatch(ChannelHandlerContext ctx, HttpContent msg) {
        boolean requestFinished = (msg instanceof LastHttpContent);
        _writeInternal(ctx, msg, requestFinished);
    }

    private void _writeInternal(final ChannelHandlerContext ctx, final HttpContent msg, boolean requestFinished) {
        if (tusByteBuffHandler == null) {
            initializeByteBuffHandler();
        }

        if (tusByteBuffHandler.hasError()) {

            OperationResult operationResult = tusByteBuffHandler.getErrorResult();

            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, operationResult.getFailedReason());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            return;
        }

        tusByteBuffHandler.addByteBuffToOrder(msg.content());

        if (requestFinished) {
            this.finalByteRead = true;

            final TusByteBuffHandler.ByteBuffHandlerEvent byteBuffHandlerEvent = new TusByteBuffHandler.ByteBuffHandlerEvent() {

                @Override
                public void onCompleted() {
                    onWriteFinished(ctx);
                }

                @Override
                public void onError(OperationResult operationResult) {
                    HttpResponse response = HttpResponseUtils
                            .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, operationResult.getFailedReason());
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            };
            tusByteBuffHandler.onFinish(byteBuffHandlerEvent);
        }
    }

    private synchronized void initializeByteBuffHandler() {
        tusByteBuffHandler = new TusByteBuffHandler(fileId, getConfiguration().getStore());
        executorService.execute(tusByteBuffHandler);
        executorService.shutdown();
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
                ReferenceCountUtil.release(msg);
            } else {
                HttpContent message = (HttpContent) msg;
                currentState = State.BODY;
                this.channelRead0(ctx, message);
            }

        } else {
            ctx.fireChannelRead(msg);
        }
    }


    public enum State {
        NEW, REQUEST, BODY
    }
}
