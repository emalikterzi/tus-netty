package com.emtdev.tus.netty.handler;

import io.netty.buffer.ByteBuf;

import java.io.InputStream;

public abstract class TusBaseInputStream extends InputStream {

    protected abstract void __closeAndWrite(ByteBuf value);

    protected abstract void __writeByte(ByteBuf value);

    protected abstract void __close();

    public final void closeAndWrite(ByteBuf value) {
        __closeAndWrite(value);
    }

    public final void close() {
        __close();
    }

    public final void writeByte(ByteBuf value) {
        __writeByte(value);
    }
}
