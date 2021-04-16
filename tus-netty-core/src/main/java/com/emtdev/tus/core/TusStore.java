package com.emtdev.tus.core;

import com.emtdev.tus.core.extension.CreationDeferLengthExtension;
import com.emtdev.tus.core.extension.CreationExtension;

/**
 * Store Marker Interface
 */
public abstract class TusStore implements CreationExtension, CreationDeferLengthExtension {

    private final TusConfigStore configStore;

    public TusStore(TusConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public final TusConfigStore configStore() {
        return this.configStore;
    }
}
