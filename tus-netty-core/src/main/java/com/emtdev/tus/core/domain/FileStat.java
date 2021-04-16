package com.emtdev.tus.core.domain;

import java.io.Serializable;

public final class FileStat implements Serializable {

    private String fileId;
    private String uploadMetaClientValue;
    private String uploadConcat;
    private String uploadDefer;
    private long uploadLength;
    private boolean partial;

    public FileStat() {
    }

    public FileStat(String fileId, long uploadLength, String uploadDefer, String uploadMetaClientValue, String uploadConcat, boolean partial) {
        this.fileId = fileId;
        this.uploadLength = uploadLength;
        this.uploadDefer = uploadDefer;
        this.uploadMetaClientValue = uploadMetaClientValue;
        this.uploadConcat = uploadConcat;
        this.partial = partial;
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

    public String getUploadConcat() {
        return uploadConcat;
    }

    public void setUploadConcat(String uploadConcat) {
        this.uploadConcat = uploadConcat;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }
}
