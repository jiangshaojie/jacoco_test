package com.laiye.performance.enity;

import lombok.Data;

@Data
public class CaseResult {
    private String id;
    private String caseUuid;
    private String taskUuid;
    private String state;
    private byte[] resultCsv;
    private byte[] resultLog;
    private String overviewData;
    private String createTime;
    private String updateTime;
    private String planTaskId;
    private String resultStringCsv;
}
