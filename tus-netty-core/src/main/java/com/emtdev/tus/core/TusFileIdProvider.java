package com.emtdev.tus.core;

import com.emtdev.tus.core.domain.TusUploadMetaData;

public interface TusFileIdProvider {

    String generateFileId(TusUploadMetaData tusUploadMetaData, boolean partial);

}
