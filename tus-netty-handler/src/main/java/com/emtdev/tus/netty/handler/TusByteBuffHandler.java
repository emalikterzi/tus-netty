package com.emtdev.tus.netty.handler;

import com.emtdev.tus.core.TusStore;
import com.emtdev.tus.core.domain.OperationResult;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TusByteBuffHandler implements Runnable {

    private final String fileId;
    private final TusStore tusStore;

    private final AtomicBoolean handlerOperational = new AtomicBoolean(true);
    private final Queue<ByteBuf> byteBufQueue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean hasError = new AtomicBoolean(false);
    private final AtomicBoolean aborted = new AtomicBoolean(false);

    private OperationResult errorResult;
    private ByteBuffHandlerEvent onFinishEvent;

    public TusByteBuffHandler(String fileId, TusStore tusStore) {
        this.fileId = fileId;
        this.tusStore = tusStore;
    }

    public void addByteBuffToOrder(ByteBuf byteBuf) {
        if (!this.handlerOperational.get()) {
            //should be never happen if a request finalized thats means there is no byteBuff anyMore
            throw new RuntimeException("ByteBuff recieved after last byte");
        }
        this.byteBufQueue.add(byteBuf);
    }

    public void onFinish(ByteBuffHandlerEvent onFinishEvent) {
        this.handlerOperational.set(false);
        this.onFinishEvent = onFinishEvent;
    }

    public boolean hasError() {
        return hasError.get();
    }

    public OperationResult getErrorResult() {
        return errorResult;
    }

    public void abort() {
        this.aborted.set(true);
    }

    private boolean aborted() {
        return aborted.get();
    }

    @Override
    public void run() {
        while (handlerOperational.get()) {

            if (hasError()) {
                break;
            }

            if (aborted()) {
                break;
            }

            if (byteBufQueue.isEmpty()) {
                sleepLittle();
                continue;
            }

            tryToWrite(byteBufQueue.poll());
        }

        if (aborted()) {
            for (ByteBuf eachBuff : byteBufQueue) {
                ReferenceCountUtil.release(eachBuff);
            }
            tusStore.finalizeFile(fileId);
            return;
        }

        while (!byteBufQueue.isEmpty()) {
            if (hasError()) {
                break;
            }

            tryToWrite(byteBufQueue.poll());
        }

        if (onFinishEvent == null) {
            //should be never happen fatal error
            throw new RuntimeException("Conflict On Byte Buff Handler Operation");
        }

        OperationResult operationResult = tusStore.finalizeFile(fileId);

        if (!hasError() && !operationResult.isSuccess()) {
            hasError.set(true);
            this.errorResult = operationResult;
        }

        if (hasError()) {
            try {
                onFinishEvent.onError(errorResult);
            } catch (Exception e) {
                //swallow
            }
        } else {
            try {
                onFinishEvent.onCompleted();
            } catch (Exception e) {
                //swallow
            }
        }

    }

    public interface ByteBuffHandlerEvent {

        void onCompleted();

        void onError(OperationResult operationResult);

    }

    private void tryToWrite(ByteBuf byteBuf) {
        OperationResult operationResult = tusStore.write(this.fileId, byteBuf);

        if (!operationResult.isSuccess()) {
            errorResult = operationResult;
            hasError.set(true);
        }

        ReferenceCountUtil.release(byteBuf);
    }

    private void sleepLittle() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
    }
}
