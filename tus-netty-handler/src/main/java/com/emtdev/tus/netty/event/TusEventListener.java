package com.emtdev.tus.netty.event;

import com.emtdev.tus.core.TusStore;

public interface TusEventListener {

    void afterCreate(String fileId, TusStore tusStore);

    void afterUploadComplete(String fileId, TusStore tusStore);

    void afterRemove(String fileId, TusStore tusStore);

}
