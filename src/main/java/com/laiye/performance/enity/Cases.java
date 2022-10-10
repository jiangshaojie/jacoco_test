package com.laiye.performance.enity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Cases {
    private String id;
    private String name;
    private String jmxUuid;
    private String config;
    private String uuid;
    private String updateTime;
    private String createTime;
    private String tagId;
    private String projectNameId;
    private String businessNameId;
}
