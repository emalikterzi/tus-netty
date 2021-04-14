package com.emtdev.tus.spring.server.custom;

import com.emtdev.tus.core.TusConfigStore;
import com.emtdev.tus.core.domain.FileStat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConfigStore implements TusConfigStore {

    private final Map<String, FileStat> inMemoryMap = new ConcurrentHashMap<String, FileStat>();

    @Override
    public void save(FileStat fileStat) {
        inMemoryMap.put(fileStat.getFileId(), fileStat);
    }

    @Override
    public void update(FileStat fileStat) {
        // no need to update since its in memory
    }

    @Override
    public FileStat get(String fileId) {
        return inMemoryMap.get(fileId);
    }
}
