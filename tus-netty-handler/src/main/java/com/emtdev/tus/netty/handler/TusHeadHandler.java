package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.FileStat;
import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import com.emtdev.tus.core.extension.CreationExtension;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

@ChannelHandler.Sharable
public class TusHeadHandler extends TusBaseRequestHandler {

    public TusHeadHandler(TusConfiguration tusConfiguration) {
        super(tusConfiguration);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {

        String fileId = resolveFileFromUri(msg.uri());

        TusStore store = getConfiguration().getStore();

        boolean exist = store.exist(fileId);

        /**
         * If the resource is not found, the Server SHOULD return either the 404 Not Found, 410 Gone or 403 Forbidden status without the Upload-Offset header.
         */
        if (!exist) {
            HttpResponse response =
                    HttpResponseUtils.createHttpResponseWithBody(HttpResponseStatus.NOT_FOUND, "PATCH request against a non-existent resource");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        long offset = store.offset(fileId);

        HttpResponse response = HttpResponseUtils.createHttpResponse(HttpResponseStatus.OK);


        /**
         * The Server MUST prevent the client and/or proxies from caching the response by adding the Cache-Control: no-store header to the response.
         *
         * The Server MUST always include the Upload-Offset header in the response for a HEAD request, even if the offset is 0.
         */
        response.headers()
                .add(HttpRequestAccessor.CACHE_CONTROL, "no-store")
                .add(HttpRequestAccessor.UPLOAD_OFFSET, offset);


        if (ExtensionUtils.supports(getConfiguration().getStore(), ExtensionUtils.Extension.CREATION_DEFER_LENGTH)) {

            CreationDeferLengthExtension creationDeferLength = (CreationDeferLengthExtension) getConfiguration().getStore();

            FileStat fileStat = creationDeferLength.configStore().get(fileId);

            /**
             * If the size of the upload is known, the Server MUST include the Upload-Length header in the response.
             */
            if (fileStat != null && fileStat.getUploadLength() > 0) {
                response.headers()
                        .add(HttpRequestAccessor.UPLOAD_LENGTH, fileStat.getUploadLength());
            } else {

                /**
                 * As long as the length of the upload is not known, the Server MUST set Upload-Defer-Length: 1 in all responses to HEAD requests.
                 */
                response.headers()
                        .add("Upload-Defer-Length", "1");
            }

            if (fileStat != null && !StringUtil.isNullOrEmpty(fileStat.getUploadMetaClientValue())) {
                response.headers()
                        .add("Upload-Metadata", fileStat.getUploadMetaClientValue());
            }

            if (fileStat != null && !StringUtil.isNullOrEmpty(fileStat.getUploadConcat())) {
                response.headers()
                        .add("Upload-Concat", fileStat.getUploadConcat());
            }

        } else {

            response.headers()
                    .add("Upload-Defer-Length", "1");
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
