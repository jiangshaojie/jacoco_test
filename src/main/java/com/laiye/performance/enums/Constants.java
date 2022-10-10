package com.laiye.performance.enums;

public enum Constants {
    TASK_STATE_RUN("run"),
    TASK_STATE_WAIT("wait"),
    TASK_STATE_SUCCESS("success"),
    TASK_STATE_FAIL("fail"),
    TASK_STATE_ABORT("abort");


    private String value;

    public String getValue() {
        return value;
    }

    Constants(String value) {
        this.value = value;
    }
}
