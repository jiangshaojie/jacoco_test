package com.laiye.performance.enity;

import lombok.Data;

@Data
public class PlanTaskRecord {
    private String id;
    private String taskId;
    private String planId;
    private String state;
    private String updateTime;
    private String executionCompletionTime;
}
