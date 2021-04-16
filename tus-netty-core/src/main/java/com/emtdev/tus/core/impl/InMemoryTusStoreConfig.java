package com.emtdev.tus.core.impl;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.domain.FileStat;
import io.netty.util.internal.PlatformDependent;

import java.util.Map;

public class InMemoryTusStoreConfig implements TusConfigStore {

    private Map<String, FileStat> fileStatMap = PlatformDependent.newConcurrentHashMap();


    @Override
    public void save(FileStat fileStat) {
        fileStatMap.put(fileStat.getFileId(), fileStat);
    }

    @Override
    public void update(FileStat fileStat) {
        // inmemory do nothing
    }

    @Override
    public FileStat get(String fileId) {
        return fileStatMap.get(fileId);
    }
}
