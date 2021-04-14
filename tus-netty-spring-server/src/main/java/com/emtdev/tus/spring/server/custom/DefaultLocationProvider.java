package com.emtdev.tus.spring.server.custom;

import com.emtdev.tus.core.TusLocationProvider;
import io.netty.handler.codec.http.HttpRequest;

public class DefaultLocationProvider implements TusLocationProvider {


    private String prefix;


    public DefaultLocationProvider(String prefix) {
        this.prefix = prefix;
    }


    @Override
    public String generateLocationHeader(HttpRequest httpRequest, String fileId) {
        return prefix + fileId;
    }
}
