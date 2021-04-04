package com.emtdev.tus.core.domain;

public class OperationResult {

    public static OperationResult SUCCESS = new OperationResult(Operation.SUCCESS, "");

    private Operation operation;
    private String failedReason;

    private OperationResult(Operation operation, String failedReason) {
        this.operation = operation;
        this.failedReason = failedReason;
    }

    public boolean isSuccess() {
        return Operation.SUCCESS.equals(this.operation);
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public static OperationResult of(Operation operation, String failedReason) {
        return new OperationResult(operation, failedReason);
    }

}
