package com.laiye.performance.enums;

public enum ConstantFileType {
    JMX_FILE_TYPE("jmx"),
    TEST_DATA_TYPE("test_data");
    private String value;

    public String getValue() {
        return value;
    }

    ConstantFileType(String value) {
        this.value = value;
    }
}
