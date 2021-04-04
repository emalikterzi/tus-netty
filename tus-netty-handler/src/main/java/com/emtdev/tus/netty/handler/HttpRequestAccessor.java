package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.domain.TusUploadMetaData;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.internal.StringUtil;

public class HttpRequestAccessor {

    public static String CACHE_CONTROL = "Cache-Control";
    public static String CONTENT_LENGTH = "Content-Length";
    public static String CONTENT_TYPE = "Content-Type";
    public static String UPLOAD_METADATA = "Upload-Metadata";
    public static String UPLOAD_OFFSET = "Upload-Offset";
    public static String UPLOAD_EXPIRES = "Upload-Expires";
    public static String UPLOAD_LENGTH = "Upload-Length";
    public static String UPLOAD_DEFER_LENGTH = "Upload-Defer-Length";
    public static String HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
    public static String UPLOAD_CONCAT = "Upload-Concat";
    public static String LOCATION = "Location";
    public static String MEDIA_TYPE_STREAM = "application/offset+octet-stream";

    private final HttpRequest fullHttpRequest;
    private TusUploadMetaData uploadMetaData;

    private HttpRequestAccessor(HttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
    }

    public static HttpRequestAccessor of(HttpRequest fullHttpRequest) {
        return new HttpRequestAccessor(fullHttpRequest);
    }

    public long getContentLength() {
        String value = fullHttpRequest.headers().get(CONTENT_LENGTH);
        if (StringUtil.isNullOrEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public TusUploadMetaData getUploadMetadata() {
        if (uploadMetaData != null) {
            return uploadMetaData;
        }

        String value = fullHttpRequest.headers().get(UPLOAD_METADATA);
        uploadMetaData = new TusUploadMetaData(value);
        return uploadMetaData;
    }

    public boolean isContentTypeOffsetStream() {
        return MEDIA_TYPE_STREAM.equals(fullHttpRequest.headers().get(CONTENT_TYPE));
    }

    public String httpMethodOverride() {
        return fullHttpRequest.headers().get(HTTP_METHOD_OVERRIDE);
    }

    public String uploadDeferLength() {
        return fullHttpRequest.headers().get(UPLOAD_DEFER_LENGTH);
    }

    public long uploadLength() {
        String value = fullHttpRequest.headers().get(UPLOAD_LENGTH);
        if (StringUtil.isNullOrEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public String getUploadConcat() {
        return fullHttpRequest.headers().get(UPLOAD_CONCAT);
    }

}
