package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.domain.FileStat;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.domain.TusUploadMetaData;
import com.emtdev.tus.core.extension.ConcatenationExtension;
import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import com.emtdev.tus.core.extension.CreationExtension;
import com.emtdev.tus.netty.event.TusEvent;
import com.emtdev.tus.netty.event.TusEventPublisher;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;

public class TusPostHandler extends TusBaseRequestBodyHandler {

    private String locationHeader;

    public TusPostHandler(TusConfiguration configuration, TusEventPublisher tusEventPublisher) {
        super(configuration, tusEventPublisher, TusNettyDecoder.POST);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) {
        _writePost(ctx, msg);
    }

    @Override
    protected void onWriteFinished(ChannelHandlerContext ctx, HttpContent msg) {
        CreationExtension creationExtension = getConfiguration().getStore();

        HttpResponse response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.CREATED);

        response.headers()
                .add(HttpRequestAccessor.LOCATION, locationHeader)
                .add(HttpRequestAccessor.UPLOAD_OFFSET, creationExtension.offset(fileId));

        addExpireHeaderToResponse(response);

        ctx.writeAndFlush(response).
                addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        HttpRequestAccessor accessor = HttpRequestAccessor.of(msg);

        if (!ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CREATION)) {
            HttpResponse response =
                    HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.PRECONDITION_FAILED, "Store not supports creation");

            ctx.writeAndFlush(response).
                    addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String uploadConcat = accessor.getUploadConcat();
        boolean partial = false;

        if (!StringUtil.isNullOrEmpty(uploadConcat)) {

            if (!ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CONCATENATION)) {
                HttpResponse response =
                        HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.PRECONDITION_FAILED, "Store not supports concatenation");

                ctx.writeAndFlush(response).
                        addListener(ChannelFutureListener.CLOSE);
                return;
            }

            uploadConcat = uploadConcat.trim();

            if (uploadConcat.equals("partial")) {
                partial = true;
            }

            if (uploadConcat.startsWith("final;")) {

                String[] files = uploadConcat.split(";")[1].split("\\s");
                String[] fileIds = new String[files.length];

                for (int i = 0; i < files.length; i++) {
                    fileIds[i] = resolveFileFromUri(files[i]);
                }

                ConcatenationExtension creation = (ConcatenationExtension) getConfiguration().getStore();

                String fileId = getConfiguration().getFileIdProvider().generateFileId(accessor.getUploadMetadata(), false);
                String locationHeader = getConfiguration().getLocationProvider().generateLocationHeader(accessor.getHttpRequest(), fileId);

                OperationResult result = creation.merge(fileId, fileIds);

                HttpResponse response;

                if (result.isSuccess()) {

                    //creation must be called
                    response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.CREATED);
                    response.headers().add(HttpRequestAccessor.LOCATION, locationHeader);

                    addExpireHeaderToResponse(response);

                    tryToSaveFileStat(fileId, accessor.getUploadMetadata(), fileIds, accessor.getUploadConcat());
                    tusEventPublisher.publishEvent(new TusEvent(fileId, TusEvent.Type.CREATE));
                } else {
                    response = HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, result.getFailedReason());
                }

                ctx.writeAndFlush(response).
                        addListener(ChannelFutureListener.CLOSE);
                return;

            }
        }

        long uploadLength = accessor.uploadLength();
        String uploadDeferLength = accessor.uploadDeferLength();

        /**
         * If the Upload-Defer-Length header contains any other value than 1 the server should return a 400 Bad Request status.
         */
        if (!StringUtil.isNullOrEmpty(uploadDeferLength) && !uploadDeferLength.equals("1")) {
            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.PRECONDITION_FAILED,
                            "Upload-Defer-Length header not valid value.");

            ctx.writeAndFlush(response).
                    addListener(ChannelFutureListener.CLOSE);
            return;
        }

        //upload length veya dfer header olmak zorunda
        if (uploadLength == 0 && StringUtil.isNullOrEmpty(uploadDeferLength)) {

            HttpResponse response = HttpResponseUtils
                    .createHttpResponseWithBody(HttpResponseStatus.PRECONDITION_FAILED,
                            "Upload-Length or Upload-Defer-Length header required");

            ctx.writeAndFlush(response).
                    addListener(ChannelFutureListener.CLOSE);
            return;
        }

        /**
         * If the length of the upload exceeds the maximum, which MAY be specified using the Tus-Max-Size header, the Server MUST respond with the 413 Request Entity Too Large status.
         */
        if (uploadLength != 0 && getConfiguration().getMaxFileSize() != 0) {

            if (uploadLength > getConfiguration().getMaxFileSize()) {

                HttpResponse response = HttpResponseUtils
                        .createHttpResponseWithBody(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                                "Request Entity Too Large");
                ctx.writeAndFlush(response).
                        addListener(ChannelFutureListener.CLOSE);
                return;
            }

        }

        long contentLength = accessor.getContentLength();

        CreationExtension creation = getConfiguration().getStore();

        fileId = getConfiguration().getFileIdProvider().generateFileId(accessor.getUploadMetadata(), partial);
        locationHeader = getConfiguration().getLocationProvider().generateLocationHeader(accessor.getHttpRequest(), fileId);

        OperationResult creationResult = creation.createFile(fileId);

        if (!creationResult.isSuccess()) {
            HttpResponse response = HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.INTERNAL_SERVER_ERROR, creationResult.getFailedReason());
            ctx.writeAndFlush(response).
                    addListener(ChannelFutureListener.CLOSE);
            return;
        }

        tryToSaveFileStat(fileId, accessor.getUploadMetadata(), uploadLength, accessor.uploadDeferLength());

        tusEventPublisher.publishEvent(new TusEvent(fileId, TusEvent.Type.CREATE));

        if (contentLength == 0) {
            //creation must be called
            HttpResponse response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.CREATED);
            response.headers()
                    .add(HttpRequestAccessor.LOCATION, locationHeader)
                    .add(HttpRequestAccessor.UPLOAD_OFFSET, "0");

            addExpireHeaderToResponse(response);

            ctx.writeAndFlush(response).
                    addListener(ChannelFutureListener.CLOSE);

        } else {

            if (!ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CREATION_WITH_UPLOAD)) {
                HttpResponse response =
                        HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.PRECONDITION_FAILED, "Store not supports creation-with-upload");

                ctx.writeAndFlush(response).
                        addListener(ChannelFutureListener.CLOSE);
                return;
            }

            /**
             * The Client MUST include the Content-Type: application/offset+octet-stream header.
             */
            if (!accessor.isContentTypeOffsetStream()) {
                HttpResponse response = HttpResponseUtils
                        .createHttpResponseWithBody(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type was not application/offset+octet-stream");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            if (msg instanceof FullHttpRequest) {
                FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
                _writePost(ctx, fullHttpRequest);
            }

        }
    }

    private void tryToSaveFileStat(String fileId, TusUploadMetaData tusUploadMetaData, String[] fileIds, String uploadConcat) {
        if (StringUtil.isNullOrEmpty(fileId)) {
            return;
        }

        String clientUploadMeta = null;

        if (tusUploadMetaData != null && !StringUtil.isNullOrEmpty(tusUploadMetaData.getClientValue())) {
            clientUploadMeta = tusUploadMetaData.getClientValue();
        }
        CreationDeferLengthExtension creationDeferLengthExtension = (CreationDeferLengthExtension) getConfiguration().getStore();

        long total = 0;

        for (String eachFile : fileIds) {
            FileStat fileStat = creationDeferLengthExtension.configStore().get(eachFile);
            total += fileStat.getUploadLength();
        }

        FileStat fileStat = new FileStat(fileId, total, null, clientUploadMeta, uploadConcat);

        creationDeferLengthExtension.configStore().save(fileStat);

    }

    private void tryToSaveFileStat(String fileId, TusUploadMetaData tusUploadMetaData, long uploadLength, String uploadDefer) {
        if (!ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CREATION_DEFER_LENGTH)) {
            return;
        }

        if (StringUtil.isNullOrEmpty(fileId)) {
            return;
        }

        String clientUploadMeta = null;

        if (tusUploadMetaData != null && !StringUtil.isNullOrEmpty(tusUploadMetaData.getClientValue())) {
            clientUploadMeta = tusUploadMetaData.getClientValue();
        }

        FileStat fileStat = new FileStat(fileId, uploadLength, uploadDefer, clientUploadMeta, null);

        CreationDeferLengthExtension creationDeferLength = (CreationDeferLengthExtension) getConfiguration().getStore();

        creationDeferLength.configStore().save(fileStat);
    }

}
