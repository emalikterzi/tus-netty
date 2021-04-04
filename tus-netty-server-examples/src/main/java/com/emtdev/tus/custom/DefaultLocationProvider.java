package com.emtdev.tus.custom;

import com.emtdev.tus.core.TusLocationProvider;

public class DefaultLocationProvider implements TusLocationProvider {


    @Override
    public String generateLocationHeader(String fileId) {
        return "http://localhost:1080/files/" + fileId;
    }
}
