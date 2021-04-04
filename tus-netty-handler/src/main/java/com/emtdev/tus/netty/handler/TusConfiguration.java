package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.TusFileIdProvider;
import com.emtdev.tus.core.TusLocationProvider;
import com.emtdev.tus.core.TusStore;

public class TusConfiguration {

    private final String contextPath;
    private final TusStore store;
    private final TusLocationProvider locationProvider;
    private final TusFileIdProvider fileIdProvider;
    private final long maxFileSize;

    public TusConfiguration(String contextPath, TusStore store, TusLocationProvider locationProvider, TusFileIdProvider fileIdProvider, long maxFileSize) {
        this.contextPath = contextPath;
        this.store = store;
        this.locationProvider = locationProvider;
        this.fileIdProvider = fileIdProvider;
        this.maxFileSize = maxFileSize;
    }

    public String getContextPath() {
        return contextPath;
    }

    public TusStore getStore() {
        return store;
    }

    public TusLocationProvider getLocationProvider() {
        return locationProvider;
    }

    public TusFileIdProvider getFileIdProvider() {
        return fileIdProvider;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }
}
