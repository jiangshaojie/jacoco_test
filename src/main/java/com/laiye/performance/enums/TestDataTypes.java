package com.laiye.performance.enums;

public enum TestDataTypes {
    FOLDER("0"),
    FILE("1");

    private String value;

    public String getValue() {
        return value;
    }

    TestDataTypes(String value) {
        this.value = value;
    }
}
