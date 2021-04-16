package com.emtdev.tus.store;

import com.amazonaws.services.s3.AmazonS3;
import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.OperationResult;
import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import io.netty.buffer.ByteBuf;

import java.io.InputStream;

public class S3Store implements TusStore, CreationDeferLengthExtension {

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
    public OperationResult write(String fileId, InputStream inputStream) {
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

    @Override
    public TusConfigStore configStore() {
        return null;
    }
}
