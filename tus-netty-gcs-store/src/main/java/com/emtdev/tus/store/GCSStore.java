package com.emtdev.tus.store;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.domain.OperationResult;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GCSStore extends FileStore {

    public GCSStore(String baseDirectory, TusConfigStore configStore) {
        super(baseDirectory, configStore);
    }

    @Override
    public OperationResult finalizeFile(String fileId) {
        return super.finalizeFile(fileId);
    }
}
