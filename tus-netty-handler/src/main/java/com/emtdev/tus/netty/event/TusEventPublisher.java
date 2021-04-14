package com.emtdev.tus.netty.event;

import com.emtdev.tus.core.TusStore;

import java.util.List;

public class TusEventPublisher {

    private final TusEventExecutorProvider tusEventExecutorProvider;
    private final List<TusEventListener> tusEventListenerList;
    private final TusStore tusStore;


    public TusEventPublisher(TusEventExecutorProvider tusEventExecutorProvider, List<TusEventListener> tusEventListenerList, TusStore tusStore) {
        this.tusEventExecutorProvider = tusEventExecutorProvider;
        this.tusEventListenerList = tusEventListenerList;
        this.tusStore = tusStore;
    }

    public void publishEvent(final TusEvent tusEvent) {
        if (tusEventExecutorProvider == null || tusStore == null || tusEventListenerList == null || tusEventListenerList.isEmpty()) {
            return;
        }

        if (tusEvent.getType().equals(TusEvent.Type.CREATE)) {
            tusEventExecutorProvider.getExecutorService().execute(new CreateRunnable(tusEvent.getFileId()));
        } else if (tusEvent.getType().equals(TusEvent.Type.DELETE)) {
            tusEventExecutorProvider.getExecutorService().execute(new DeleteRunnable(tusEvent.getFileId()));
        } else if (tusEvent.getType().equals(TusEvent.Type.UPDATE_COMPLETED)) {
            tusEventExecutorProvider.getExecutorService().execute(new CompleteRunnable(tusEvent.getFileId()));
        }

    }

    private class CreateRunnable implements Runnable {

        private final String fileId;

        public CreateRunnable(String fileId) {
            this.fileId = fileId;
        }

        @Override
        public void run() {

            for (TusEventListener each : tusEventListenerList) {
                try {
                    each.afterCreate(fileId, tusStore);
                } catch (Exception e) {
                    //swallow
                }
            }

        }
    }

    private class CompleteRunnable implements Runnable {

        private final String fileId;

        public CompleteRunnable(String fileId) {
            this.fileId = fileId;
        }

        @Override
        public void run() {

            for (TusEventListener each : tusEventListenerList) {
                try {
                    each.afterUploadComplete(fileId, tusStore);
                } catch (Exception e) {
                    //swallow
                }
            }

        }
    }

    private class DeleteRunnable implements Runnable {

        private final String fileId;

        public DeleteRunnable(String fileId) {
            this.fileId = fileId;
        }

        @Override
        public void run() {

            for (TusEventListener each : tusEventListenerList) {
                try {
                    each.afterRemove(fileId, tusStore);
                } catch (Exception e) {
                    //swallow
                }
            }

        }
    }
}
