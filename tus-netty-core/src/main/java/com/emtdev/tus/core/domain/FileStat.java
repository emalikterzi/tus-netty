package com.emtdev.tus.core.domain;

import java.io.Serializable;

public final class FileStat implements Serializable {

    private String fileId;
    private long uploadLength;
    private String uploadDefer;
    private String uploadMetaClientValue;
    private long uploadOffset = 0;
    private String uploadConcat;

    public FileStat(String fileId, long uploadLength, String uploadDefer, String uploadMetaClientValue, String uploadConcat) {
        this.fileId = fileId;
        this.uploadLength = uploadLength;
        this.uploadDefer = uploadDefer;
        this.uploadMetaClientValue = uploadMetaClientValue;
        this.uploadConcat = uploadConcat;
    }

    public String getUploadDefer() {
        return uploadDefer;
    }

    public void setUploadDefer(String uploadDefer) {
        this.uploadDefer = uploadDefer;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getUploadLength() {
        return uploadLength;
    }

    public void setUploadLength(long uploadLength) {
        this.uploadLength = uploadLength;
    }


    public String getUploadMetaClientValue() {
        return uploadMetaClientValue;
    }

    public void setUploadMetaClientValue(String uploadMetaClientValue) {
        this.uploadMetaClientValue = uploadMetaClientValue;
    }

    public long getUploadOffset() {
        return uploadOffset;
    }

    public void setUploadOffset(long uploadOffset) {
        this.uploadOffset = uploadOffset;
    }

    public String getUploadConcat() {
        return uploadConcat;
    }

    public void setUploadConcat(String uploadConcat) {
        this.uploadConcat = uploadConcat;
    }
}
