package com.emtdev.tus.core;

import io.netty.handler.codec.http.HttpRequest;

public interface TusLocationProvider {

    String generateLocationHeader(HttpRequest httpRequest, String fileId);

}
