package com.emtdev.tus.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TusOrderInputStream extends TusBaseInputStream {

    private BlockingQueue<ByteBuf> byteBuffs = new LinkedBlockingQueue<>();

    private final Object __locker = new Object();
    private boolean __locked = false;
    private boolean __closed = false;


    @Override
    public int read() throws IOException {
        ByteBuf byteBuf = selectNext();
        if (byteBuf == null) {
            System.out.println("read complated");
            return -1;
        }

        if (byteBuf.isReadable()) {
            /**
             * @see io.netty.buffer.ByteBufInputStream#read()
             */
            return byteBuf.readByte() & 0xff;
        } else {
            //completed release
            ReferenceCountUtil.release(byteBuf);
            byteBuffs.remove(byteBuf);
            return this.read();
        }
    }

    private ByteBuf selectNext() {
        synchronized (__locker) {

            while (!__closed) {
                ByteBuf byteBuf = byteBuffs.peek();

                if (byteBuf == null) {
                    tryToLock();
                    continue;
                }

                return byteBuf;
            }

            return byteBuffs.peek();
        }
    }

    public void __writeByte(ByteBuf value) {
        synchronized (__locker) {
            byteBuffs.add(value);
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
            byteBuffs.add(value);
            __closed = true;
            tryToUnlock();
        }
    }

    private void tryToLock() {
        if (!__locked) {
            __locked = true;
            try {
                System.out.println("locklandi");
                __locker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void tryToUnlock() {
        if (__locked) {
            __locked = false;
            System.out.println("devam"
            );
            __locker.notifyAll();
        }
    }

}
