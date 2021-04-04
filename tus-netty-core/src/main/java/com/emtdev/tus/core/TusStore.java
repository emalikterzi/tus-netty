package com.emtdev.tus.core;

import com.emtdev.tus.core.extension.CreationExtension;

/**
 * Store Marker Interface
 */
public interface TusStore extends CreationExtension {

    void afterConnectionClose(String fileId);

    void onException(String fileId, Throwable e);

}
