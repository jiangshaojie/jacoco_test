package com.laiye.performance.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class RespResult<T> implements Serializable {
    T data;
    private int code;
}
