package com.emtdev.tus.netty.handler;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;

public class TusInputStream extends InputStream {


    private final ByteBuf byteBuf;
    private final Object __locker = new Object();
    private boolean __locked = false;
    private boolean __closed = false;

    public TusInputStream(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public int read() throws IOException {
        synchronized (__locker) {

            if (!__closed && !byteBuf.isReadable()) {
                tryToLock();
            }


            if (byteBuf.isReadable()) {
                /**
                 * @see io.netty.buffer.ByteBufInputStream#read()
                 */
                return byteBuf.readByte() & 0xff;
            }
            return -1;

        }
    }

    public void __writeByte(ByteBuf value) {
        synchronized (__locker) {
            byteBuf.writeBytes(value);
            tryToUnlock();
        }
    }

    public void __close() {
        synchronized (__locker) {
            __closed = true;
            tryToUnlock();
        }
    }

    public void __closeAndWrite(ByteBuf value) {
        synchronized (__locker) {
            byteBuf.writeBytes(value);
            __closed = true;
            tryToUnlock();
        }
    }

    private void tryToLock() {
        if (!__locked) {
            __locked = true;
            try {
                __locker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void tryToUnlock() {
        if (__locked) {
            __locked = false;
            __locker.notifyAll();
        }
    }

}
