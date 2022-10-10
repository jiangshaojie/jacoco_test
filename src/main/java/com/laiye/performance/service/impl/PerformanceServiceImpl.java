package com.laiye.performance.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laiye.performance.dao.*;
import com.laiye.performance.enity.CasePlan;
import com.laiye.performance.enity.CaseResult;
import com.laiye.performance.enity.PlanTaskRecord;
import com.laiye.performance.enums.Constants;
import com.laiye.performance.model.RespResult;
import com.laiye.performance.model.SampleData;
import com.laiye.performance.service.PerformanceService;
import com.laiye.performance.service.TaskService;
import com.laiye.performance.utils.Common;
import com.laiye.performance.utils.OperationFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTRad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PerformanceServiceImpl implements PerformanceService {
    @Autowired
    TaskService taskService;
    @Autowired
    OperationTaskState operationTaskState;
    @Autowired
    Common common;
    @Autowired
    OperationCasePlan operationCasePlan;
    @Autowired
    OperationCasesResult operationCasesResult;
    @Autowired
    OperationFile operationFile;
    @Autowired
    OperationCasesResultHistory operationCasesResultHistory;
    @Autowired
    OperationPlanTaskRecord operationPlanTaskRecord;
    @Value("${jmeter.binPath}")
    private String jmeterPath;

    //    @Override
    public RespResult executeTaskPlanTaskId(String caseUuid, String planTaskId, JSONObject planParam) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String rootDir = System.getProperty("user.dir");
        log.info("rootDir: {}", rootDir);
        operationCasesResult.deleteCaseResult(caseUuid);
        operationTaskState.insertTaskState(uuid, Constants.TASK_STATE_WAIT.getValue(), common.getUpdateTime());
        CaseResult caseResult = new CaseResult();
        caseResult.setCaseUuid(caseUuid);
        caseResult.setTaskUuid(uuid);
        caseResult.setUpdateTime(common.getUpdateTime());
        caseResult.setState(Constants.TASK_STATE_WAIT.getValue());
        caseResult.setPlanTaskId(planTaskId);
        operationCasesResult.insertCasesResult(caseResult);
        operationCasesResultHistory.deleteCasesResultHistory(caseResult);//如果有相同的plan_task_id记录则先删除
        operationCasesResultHistory.insertCasesResult(caseResult);
        taskService.executeJmeterTask(caseUuid, rootDir, uuid, planTaskId, planParam);
        RespResult result = new RespResult();
        result.setCode(1);
        JSONObject jsonObject = new JSONObject();
        result.setData(jsonObject);
        jsonObject.put("message", "success");
        return result;
    }

    @Override
    public RespResult executeTask(String caseUuid, String planTaskId, String planId) {
        JSONObject planParam = new JSONObject();
        if (!StringUtils.isEmpty(planId)) {
            CasePlan casePlan = operationCasePlan.queryCasePlanById(planId);
            try {
                planParam = JSONObject.parseObject(casePlan.getParams());
            } catch (Exception e) {
                log.info("plan 解析参数失败：{}", planId);
            }
        }
        RespResult respResult = executeTaskPlanTaskId(caseUuid, planTaskId, planParam);
        return respResult;
    }

    @Override
    public RespResult executeCasePlan(String planId, String projectName) {
        RespResult respResult = new RespResult();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        CasePlan casePlan = operationCasePlan.queryCasePlan(planId, projectName);
        JSONArray jsonArray = JSONArray.parseArray(casePlan.getCasesIds());
        String planTaskId = UUID.randomUUID().toString().replaceAll("-", "");
        PlanTaskRecord planTaskRecord = new PlanTaskRecord();
        planTaskRecord.setPlanId(planId);
        planTaskRecord.setTaskId(planTaskId);
        planTaskRecord.setUpdateTime(common.getUpdateTime());
        planTaskRecord.setState("wait");
        operationPlanTaskRecord.insertPlanTaskRecord(planTaskRecord);
        JSONObject planParam;
        try {
            planParam = JSONObject.parseObject(casePlan.getParams());
        } catch (Exception e) {
            log.info("plan解析参数失败：{}", casePlan.getId());
            planParam = new JSONObject();
        }
        for (Object id : jsonArray) {
            executeTaskPlanTaskId(id.toString(), planTaskId, planParam);
        }
//        operationTaskRecord.updatePlanTaskDoneState(planTaskId, "success", common.getUpdateTime(), "run");
        jsonObject.put("message", "success");
        return respResult;
    }

    @Override
    public RespResult queryCaseResult(String name, String caseUuid) {
        RespResult result = new RespResult();
        result.setCode(0);
        CaseResult caseResult = operationCasesResult.queryCasesResult(name, caseUuid);
        JSONObject overJsonData = JSONObject.parseObject(caseResult.getOverviewData());
        for (Map.Entry<String, Object> entry : overJsonData.entrySet()) {
            overJsonData.put(entry.getKey(), JSONObject.parseObject(entry.getValue().toString(), SampleData.class).getJsonData());
        }
        result.setData(overJsonData);
        return result;
    }

    @Override
    public RespResult queryCaseLog(String name, String caseUuid) {
        RespResult result = new RespResult();
        result.setCode(0);
        CaseResult caseResult = operationCasesResult.queryCasesResult(name, caseUuid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", caseResult.getResultLog());
        result.setData(jsonObject);
        return result;
    }

    @Override
    public RespResult queryNativeReport(String taskId) {
//        CaseResult caseResult = operationCasesResult.queryCasesResult(name, caseUuid);
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(1);
        result.setData(jsonObject);
        CaseResult caseResult = operationCasesResultHistory.queryCasesResultByTaskId(taskId);
        byte[] unZipCsv = caseResult.getResultCsv();
//        String log = operationFile.unzipString(unZipLog);
        String csv = null;
        try {
            csv = operationFile.uncompress(unZipCsv);
        } catch (IOException e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
        //后续csv文件做了个压缩，因此csv文件做了判断，进行了两种生成原生报告形式
        boolean isOldDataType = false;
        if (StringUtils.isEmpty(csv)) {
            isOldDataType = true;
            csv = caseResult.getResultStringCsv();
        }
//        String csv = caseResult.getResultCsv();
        String rootDir = System.getProperty("user.dir");
        String reportFolderPath = rootDir + "/data/report/" + caseResult.getTaskUuid();
        String path = reportFolderPath + "/result.csv";
        File file = new File(path);
        File file1 = file.getParentFile();
        if (file1.exists()) {
            log.info("report exist");
            jsonObject.put("message", "success");
            return result;
        }
//        operationFile.deleteFile(file1.getAbsolutePath());
        try {
            file1.mkdirs();
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            if (isOldDataType == true) {
                String[] content = csv.split("/n");
                for (int i = 0; i < content.length; i++) {
                    bufferedWriter.write(content[i]);
                    bufferedWriter.newLine();
                }
            } else {
                bufferedWriter.write(csv);
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(jmeterPath);
        commandBuilder.append(" -g result.csv -o report ");
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        StringBuilder processResult = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(commandBuilder.toString(), null, file1);
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = bufrIn.readLine()) != null) {
                processResult.append(line).append(System.lineSeparator());
                if (line.equals("errorlevel=1")) {
                    process.destroy();
                }
            }
            while ((line = bufrError.readLine()) != null) {
                processResult.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            closeStream(bufrIn);
            closeStream(bufrError);
        }
        log.info("generate jmeter result:{}", processResult);
        jsonObject.put("message", "success");
        return result;
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
}
