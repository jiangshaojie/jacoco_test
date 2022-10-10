package com.laiye.performance.service;

import com.laiye.performance.model.RespResult;

public interface PerformanceService {
    RespResult executeTask(String caseUuid, String planTaskId,String planId);

    RespResult executeCasePlan( String id, String projectName);

    RespResult queryCaseResult(String name, String caseUuid);

    RespResult queryCaseLog(String name, String caseUuid);

    RespResult queryNativeReport(String taskId);
}
