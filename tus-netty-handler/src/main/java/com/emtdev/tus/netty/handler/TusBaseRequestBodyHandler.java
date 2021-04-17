package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.domain.FileStat;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.CreationExtension;
import com.emtdev.tus.core.extension.CreationWithUploadExtension;
import com.emtdev.tus.core.extension.ExpirationExtension;
import com.emtdev.tus.netty.event.TusEventPublisher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class TusBaseRequestBodyHandler extends ChannelInboundHandlerAdapter {


    private final ReentrantLock lock = new ReentrantLock();
    private final Object locker = new Object();
    private final TusConfiguration configuration;
    protected final TusEventPublisher tusEventPublisher;
    protected final String httpMethod;
    protected State currentState = State.NEW;

    protected String fileId;
    private ExecutorService executorService;
    private TusBaseInputStream inputStream;
    private boolean writerInitialized = false;
    private Callable<OperationResult> operationResultCallable;
    private Future<OperationResult> operationResultFuture;

    public TusBaseRequestBodyHandler(TusConfiguration configuration, TusEventPublisher tusEventPublisher, String httpMethpd) {
        this.configuration = configuration;
        this.tusEventPublisher = tusEventPublisher;
        this.httpMethod = httpMethpd;
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
        if (this.currentState == State.BODY) {
            if (inputStream != null && writerInitialized) {
                inputStream.close();
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        try {
//            getConfiguration().getStore().onException(fileId, cause);
//        } catch (Exception e) {
//        }
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
        final CreationWithUploadExtension creationExtension = (CreationWithUploadExtension) getConfiguration().getStore();
        synchronized (locker) {
            inputStream = new TusOrderInputStream();
            operationResultCallable = new ForPost(fileId, inputStream, creationExtension);
            tryToInitializeWriter();
        }
        _writeInternal(ctx, msg, requestFinished);
    }

    public void _writePatch(ChannelHandlerContext ctx, HttpContent msg) {
        boolean requestFinished = (msg instanceof LastHttpContent);
        final CreationExtension creationExtension = getConfiguration().getStore();

        synchronized (locker) {
            if (operationResultCallable == null) {
                inputStream = new TusOrderInputStream();
                operationResultCallable = new ForPatch(fileId, inputStream, creationExtension);
                tryToInitializeWriter();
            }
        }

        _writeInternal(ctx, msg, requestFinished);
    }

    private void tryToInitializeWriter() {
        if (this.writerInitialized) {
            return;
        }
        this.writerInitialized = true;
        executorService = Executors.newSingleThreadExecutor();
        operationResultFuture = executorService.submit(operationResultCallable);
        executorService.shutdown();
    }

    private class ForPost implements Callable<OperationResult> {

        private final String fileId;
        private final InputStream inputStream;
        private final CreationWithUploadExtension creationExtension;

        public ForPost(String fileId, InputStream inputStream, CreationWithUploadExtension creationExtension) {
            this.fileId = fileId;
            this.inputStream = inputStream;
            this.creationExtension = creationExtension;
        }


        @Override
        public OperationResult call() throws Exception {
            return creationExtension.createAndWrite(fileId, inputStream);
        }
    }


    private class ForPatch implements Callable<OperationResult> {

        private final String fileId;
        private final InputStream inputStream;
        private final CreationExtension creationExtension;

        public ForPatch(String fileId, InputStream inputStream, CreationExtension creationExtension) {
            this.fileId = fileId;
            this.inputStream = inputStream;
            this.creationExtension = creationExtension;
        }


        @Override
        public OperationResult call() throws Exception {
            return creationExtension.write(fileId, inputStream);
        }
    }

    private void beforeFinished() {

        FileStat fileStat = configuration.getStore().configStore().get(fileId);
        if (fileStat == null)
            return;

        if (fileStat.isPartial())
            return;

        long totalLength = fileStat.getUploadLength();
        long fileLength = configuration.getStore().offset(fileId);

        if (totalLength == fileLength) {
            //should be finalize
            getConfiguration().getStore().finalizeFile(fileId);
        }
    }


    private void _writeInternal(ChannelHandlerContext ctx, HttpContent msg, boolean requestFinished) {

        /**
         * request body tamamlanmadan yazma asamasinda bir problem ciktiysa buraya girer input stream i direk kapatip
         * hata donuyoruz
         */
        if (!requestFinished && this.operationResultFuture.isDone()) {

            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Operation Result Failed");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            inputStream.close();

            return;

        } else if (!requestFinished) {
            ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.directBuffer();
            byteBuf.writeBytes(msg.content());
            inputStream.writeByte(byteBuf);
        } else {
            /**
             * son body geldigine gore input stream i kapatip son byte body i yaziyoruz
             */
            ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.directBuffer();
            byteBuf.writeBytes(msg.content());
            inputStream.closeAndWrite(byteBuf);

            OperationResult operationResult;
            try {
                operationResult = this.operationResultFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                HttpResponse response = HttpResponseUtils
                        .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Operation Result Failed On Last Byte");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            if (operationResult.isSuccess()) {
                beforeFinished();
                onWriteFinished(ctx, msg);
            } else {
                HttpResponse response = HttpResponseUtils
                        .createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Operation Result Failed");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
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
                    currentState = State.REQUEST;
                    this.channelRead0(ctx, message);
                } else {
                    HttpContent message = (HttpContent) msg;
                    currentState = State.BODY;
                    this.channelRead0(ctx, message);
                }
                ReferenceCountUtil.release(msg);
            } finally {
                lock.unlock();
            }

        } else {
            ctx.fireChannelRead(msg);
        }
    }


    public enum State {
        NEW, REQUEST, BODY
    }
}
