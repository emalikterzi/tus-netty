package com.emtdev.tus.netty.handler;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

public class HttpResponseUtils {
    public static HttpResponse createHttpResponse(HttpResponseStatus httpResponseStatus) {
        return createHttpResponseWithBody(httpResponseStatus, null);
    }

    public static HttpResponse createHttpResponseWithBody(HttpResponseStatus httpResponseStatus, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
        if (!StringUtil.isNullOrEmpty(body)) {
            response.content().writeBytes(body.getBytes());
        }
        return response;
    }
}
