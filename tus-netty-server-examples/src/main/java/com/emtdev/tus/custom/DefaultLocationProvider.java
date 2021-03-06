package com.emtdev.tus.custom;

import com.emtdev.tus.core.TusLocationProvider;
import io.netty.handler.codec.http.HttpRequest;

public class DefaultLocationProvider implements TusLocationProvider {


    @Override
    public String generateLocationHeader(HttpRequest httpRequest, String fileId) {
        return "http://localhost:1080/files/" + fileId;
    }
}
