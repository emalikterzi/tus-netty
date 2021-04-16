package com.emtdev.tus.store;

import com.amazonaws.services.s3.AmazonS3;
import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.domain.Operation;
import com.emtdev.tus.core.domain.OperationResult;


public class S3Store extends FileStore {

    private final AmazonS3 s3;
    private final String bucketName;

    public S3Store(String baseDirectory, TusConfigStore configStore, AmazonS3 s3, String bucketName) {
        super(baseDirectory, configStore);
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public OperationResult finalizeFile(String fileId) {
        try {
            s3.putObject(bucketName, fileId, getFile(fileId));
            return OperationResult.SUCCESS;
        } catch (Exception e) {
            return OperationResult.of(Operation.FAILED, e.toString());
        }
    }


}
