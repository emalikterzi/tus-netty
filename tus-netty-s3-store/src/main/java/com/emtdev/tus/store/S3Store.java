package com.emtdev.tus.store;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.Operation;
import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;

public class S3Store implements TusStore {

    private final AmazonS3 s3;
    private final String bucketName;

    public S3Store(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }


    @Override
    public OperationResult createFile(String fileId) {
        return OperationResult.SUCCESS;
    }

    @Override
    public OperationResult write(String fileId, ByteBuf byteBuf, boolean finalBytes) {


        return null;
    }

    @Override
    public boolean exist(String fileId) {
        return s3.doesObjectExist(bucketName, fileId);
    }

    @Override
    public long offset(String fileId) {
        return -1;
    }

    @Override
    public void afterConnectionClose(String fileId) {

    }

    @Override
    public void onException(String fileId, Throwable e) {

    }

}
