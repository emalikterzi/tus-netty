package com.emtdev.tus.netty.event;

public class TusEvent {

    private final String fileId;
    private final Type type;

    public TusEvent(String fileId, Type type) {
        this.fileId = fileId;
        this.type = type;
    }


    public String getFileId() {
        return fileId;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        CREATE, DELETE, UPDATE_COMPLETED
    }


}
