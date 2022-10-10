
package com.laiye.performance.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laiye.performance.enity.ProjectManagement;
import com.laiye.performance.model.RespResult;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface PerformanceDataService {
    public void getJmxFile();

    public RespResult uploadJmxFile(MultipartFile file, String projectName, String businessName, boolean isOverwritingUpload);

    RespResult insertProjectManagement(String name);

    public ProjectManagement queryProjectManagementById(String id);

    public RespResult addCases(String caseName, String jmxUuid, String projectName, String businessName, JSONObject config, JSONArray tags);

    public RespResult updateCases(String caseName, String caseUuid, String jmxUuid, JSONObject config);

    RespResult addPlan(String name, String projectName, String businessName, JSONArray casesIds, JSONObject planParam);

    String queryCases(String projectName, String businessName);

    RespResult queryPlan(String projectName, String businessName);

    RespResult queryProject();

    RespResult queryJmxFile(String name, String businessName);

    RespResult uploadTestData(MultipartFile[] folder, String name, String bussinessName);

    RespResult queryTestData(String projectName);

    RespResult queryCaseResultHistory(String caseUuid);

    RespResult deleteJmxFile(String jmxUuid);

    RespResult queryPlanCases(String planId);

    RespResult addPane(String projectName, String businessName, String category);

    RespResult queryProjectName();

    RespResult queryCaseLog(String uuid);

    RespResult queryCaseHistoryLog(String uuid);

    RespResult queryPlanHistory(String planId);

    RespResult queryPlanCaseResult(String planId, String planTaskId);

    RespResult queryPlanCasesAndOptionalCase(String planId);

    RespResult updatePlan(String planName, String planId, JSONArray casesIds, JSONObject planParam);

    RespResult updateCase(String name, String caseUuid, JSONObject config, String jmxUuid, JSONArray tags);

    XSSFWorkbook planPerformanceResultDownLoad(String planId, String planTaskId);

    RespResult preEditCheckCase(String caseUuid);

    RespResult checkJmxFileIsExist(String fileName, String projectName, String bussinessName);

    RespResult copyCase(String caseUuid);

    File downloadJmxFile(String projectName, String jmxFileName);

    RespResult abortTask(String taskId);


    RespResult queryTask();

    RespResult manualSyncData();

    RespResult addTag(String name, String projectName, String tabName, String category);

    RespResult queryTag(String projectName, String tabName, String category);

    RespResult deleteTag(String id);

}
