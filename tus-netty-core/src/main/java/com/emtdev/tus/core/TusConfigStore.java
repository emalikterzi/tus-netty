package com.emtdev.tus.core;

import com.emtdev.tus.core.domain.FileStat;

public interface TusConfigStore {

    void save(FileStat fileStat);

    void update(FileStat fileStat);

    FileStat get(String fileId);

}
