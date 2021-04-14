package com.emtdev.tus.spring.server.custom;

import com.emtdev.tus.core.TusFileIdProvider;
import com.emtdev.tus.core.domain.TusUploadMetaData;

import java.util.UUID;

public class DefaultFileIdGenerator implements TusFileIdProvider {

    @Override
    public String generateFileId(TusUploadMetaData tusUploadMetaData, boolean partial) {
        return UUID.randomUUID().toString();
    }

}
