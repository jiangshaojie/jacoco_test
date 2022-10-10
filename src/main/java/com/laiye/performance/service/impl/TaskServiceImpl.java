package com.laiye.performance.service.impl;

import com.alibaba.fastjson.JSONObject;
//import com.laiye.performance.component.TaskManagement;
import com.laiye.performance.component.TaskManagement;
import com.laiye.performance.dao.*;
import com.laiye.performance.enity.CaseResult;
import com.laiye.performance.enity.Cases;
import com.laiye.performance.enity.JmxFileDescription;
import com.laiye.performance.enity.ProjectManagement;
import com.laiye.performance.enums.Constants;
import com.laiye.performance.service.TaskService;
import com.laiye.performance.utils.Common;
import com.laiye.performance.utils.OSSUtil;
import com.laiye.performance.utils.OperationFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    OperationCases operationCases;
    @Autowired
    OperationJmxFileDescription operationJmxFileDescription;
    @Autowired
    OperationTaskState operationTaskState;
    @Autowired
    Common common;
    @Autowired
    OperationCasesResult operationCasesResult;
    @Autowired
    OperationCasesResultHistory operationCasesResultHistory;
    @Value("${jmeter.binPath}")
    private String jmeterPath;
    @Autowired
    OperationFile operationFile;
    @Autowired
    OperationPlanTaskRecord operationPlanTaskRecord;
    @Autowired
    OperationProcess operationProcess;
    @Autowired
    TaskManagement taskManagement;
    @Autowired
    PerformanceDataServiceImpl performanceDataService;
    @Autowired
    OperationProjectManagement operationProjectManagement;

    @Override
    @Async("jmeterTaskExecutor")
    public void executeJmeterTask(String caseUuid, String rootDir, String taskUuid, String planTaskId, JSONObject planParam) {
        int re = performanceDataService.checkOrUpdateTaskState(taskUuid, false);
        log.info("任务执行前状态查询 {} {}", taskUuid, re);
        if (re != 1) {
            log.info("任务查询状态不满足条件，任务跳过执行");
            return;
        }
        try {
            log.info("等待30秒，启动任务：");
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //防止任务等待中，被停止继续运行，因此执行前再检查一遍
        re = performanceDataService.checkOrUpdateTaskState(taskUuid, false);
        log.info("jmeter任务启动前状态查询 {} {}", taskUuid, re);
        if (re != 1) {
            log.info("jmeter任务查询状态不满足条件，jmeter任务跳过执行: {}", taskUuid);
            return;
        }
        if (planTaskId != null) {
            operationPlanTaskRecord.updatePlanTaskState(planTaskId, Constants.TASK_STATE_RUN.getValue(),
                    common.getUpdateTime(), Constants.TASK_STATE_WAIT.getValue());
        }
        operationTaskState.updateTaskState(taskUuid, Constants.TASK_STATE_RUN.getValue(), common.getUpdateTime());
        CaseResult caseResult = new CaseResult();
        caseResult.setCaseUuid(caseUuid);
        caseResult.setTaskUuid(taskUuid);
        caseResult.setState(Constants.TASK_STATE_RUN.getValue());
        caseResult.setCreateTime(common.getUpdateTime());
        caseResult.setUpdateTime(common.getUpdateTime());
        operationCasesResult.updateCasesResult(caseResult);
        operationCasesResultHistory.updateCasesResultHistory(caseResult);
        Cases casesExecute = operationCases.queryCasesById(caseUuid);
        JmxFileDescription jmxFileDescription = operationJmxFileDescription.queryJmxFileByUuid(casesExecute.getJmxUuid());
        JSONObject config = (JSONObject) JSONObject.parse(casesExecute.getConfig());
        String resultFolder = rootDir + "/data/" + Thread.currentThread().getName();
        deleteFile(resultFolder);
        String csvPath = resultFolder + "/result.csv";
        String logPath = resultFolder + "/result.log";
        String resultPath = resultFolder + "/result";
        List<String> commandList = new ArrayList<>();
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(jmeterPath);
        commandBuilder.append(" -n -t ");
        commandBuilder.append(jmxFileDescription.getName());
        commandBuilder.append(" -l ").append(csvPath).append(" -j ").append(logPath).append(" -e -o ").append(resultPath).append(" ");
        commandList.add(jmeterPath);
        commandList.add("-n");
        commandList.add("-t");
        commandList.add(jmxFileDescription.getName());
        commandList.add("-l");
        commandList.add(csvPath);
        commandList.add("-j");
        commandList.add(logPath);
        commandList.add("-e");
        commandList.add("-o");
        commandList.add(resultPath);
//        commandList.add(" ");
        if (config.size() > 0) {
//            String configParam = joinConfig(config, planParam);
//            commandBuilder.append(configParam);
            commandList.addAll(joinConfig(config, planParam));
        }

//        Object maximumExecutionTime = config.getOrDefault("maximumExecutionTime", 30);
        log.info("task thread name: {} {}", taskUuid, Thread.currentThread().getName());
//        log.info("task maximumExecutionTime: {}", maximumExecutionTime);
        String command = commandBuilder.toString();
//        log.info("task command: {} {}", taskUuid, command);
        log.info("task command: {} {}", taskUuid, commandList);
        ProjectManagement projectManagement = operationProjectManagement.queryProjectById(jmxFileDescription.getProjectNameId());
        String runDir = "data/" + projectManagement.getName() + "/";
        String jmxFilePath = runDir + jmxFileDescription.getName();
        if (!operationFile.isFileExist(jmxFilePath)) {
            OSSUtil.downloadFolder(jmxFilePath, jmxFilePath);
        }
       /* if (!StringUtils.isEmpty(jmxFileDescription.getBusinessName())) {
            runDir = runDir + jmxFileDescription.getBusinessName() + "/";
        }*/
        log.info("execute jmeter runDir:{} {}", taskUuid, runDir);
        File dir = new File(runDir);
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
//        StringBuilder result = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(commandList.toArray(new String[0]), null, dir);
//            operationProcess.destroyProcess(process, Long.parseLong(maximumExecutionTime.toString()), taskUuid);
            taskManagement.addTaskIdProcess(taskUuid, process); //加入task管理，提供手动终止任务
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            operationProcess.bufferReaderLog(bufrIn, taskUuid, process, "info");
            operationProcess.bufferReaderLog(bufrError, taskUuid, process, "error");
            int status = process.waitFor();

//            String line;
           /* while ((line = bufrIn.readLine()) != null) {
                log.info("in line: {} {}", taskUuid, line);
                result.append(line).append(System.lineSeparator());
                if (line.equals("errorlevel=1")) {
                    process.destroy();
                }
            }
            while ((line = bufrError.readLine()) != null) {
                log.info("err line: {} {}", taskUuid, line);
                result.append(line).append(System.lineSeparator());
            }*/
            log.info("task {}, status {}", taskUuid, status);
            log.info("task {}, status {}, Alive {}", taskUuid, process.exitValue(), process.isAlive());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
                log.info("task destroy: {}", taskUuid);
            }
            taskManagement.deleteTaskIdProcess(taskUuid);
            closeStream(bufrIn);
            closeStream(bufrError);
        }
//        log.info("task executeJmeter result:{} {}", taskUuid, result);
        String resultLog = getFileString1(logPath);
        if (resultLog.contains("Dashboard generated")) {
            operationTaskState.updateTaskState(taskUuid, Constants.TASK_STATE_SUCCESS.getValue(), common.getUpdateTime());
            caseResult.setState(Constants.TASK_STATE_SUCCESS.getValue());
        } else {
            caseResult.setState(Constants.TASK_STATE_FAIL.getValue());
            operationTaskState.updateTaskState(taskUuid, Constants.TASK_STATE_FAIL.getValue(), common.getUpdateTime());
        }
        String resultCsv = getFileString(csvPath);
        String overView = getFileString1(resultPath + "/statistics.json");
//        CaseResult caseResult = new CaseResult();
        caseResult.setCaseUuid(caseUuid);
        caseResult.setTaskUuid(taskUuid);
        caseResult.setUpdateTime(common.getUpdateTime());
//        caseResult.setResultCsv(resultCsv);
        try {
            caseResult.setResultCsv(operationFile.compress(resultCsv.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            caseResult.setResultLog(operationFile.compress(resultLog.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        caseResult.setOverviewData(overView);
        int insertCaseResult = operationCasesResult.updateCasesResult(caseResult);
        caseResult.setPlanTaskId(planTaskId);
        if (!StringUtils.isEmpty(planTaskId)) {
            updatePlanTaskDone(planTaskId);
        }
//        operationCasesResultHistory.insertCasesResult(caseResult);
        int insertCaseResultHistory = operationCasesResultHistory.updateCasesResultHistory(caseResult);
        log.info("task insert case_result: {} {}", taskUuid, insertCaseResult);
        log.info("task insert case_result_history: {} {}", taskUuid, insertCaseResultHistory);
        log.info("executeJmeter task done: {}", taskUuid);
        try {
            log.info("等待30秒，执行下个任务：");
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private void bufferReaderLog(BufferedReader bufferedReader, String taskUuid, Process process, String logType) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.info("{}: {} {}", logType, taskUuid, line);
            if (line.equals("errorlevel=1")) {
                log.info("errorLevel destroy: {}", taskUuid);
                process.destroy();
            }
        }
    }

    private List<String> joinConfig(JSONObject config, JSONObject planParam) {
//        StringBuilder commandBuilder = new StringBuilder();
        List<String> commandList = new ArrayList<>();
        if (config.containsKey("localJMeterproperty")) {
            JSONObject localJMeterproperty = config.getJSONObject("localJMeterproperty");
            Set<Map.Entry<String, Object>> entry = localJMeterproperty.entrySet();
            for (Map.Entry<String, Object> entry1 : entry) {
                if (planParam.containsKey(entry1.getKey())) {
                    commandList.add("-J" + entry1.getKey() + "=" + planParam.getString(entry1.getKey()));
//                    commandBuilder.append("-J").append(entry1.getKey()).append("=").append(planParam.getString(entry1.getKey())).append(" ");
                } else {
//                    commandBuilder.append("-J").append(entry1.getKey()).append("=").append(entry1.getValue().toString()).append(" ");
                    commandList.add("-J" + entry1.getKey() + "=" + entry1.getValue().toString());
                }
            }
        }
        if (config.containsKey("remoteJMeterproperty")) {
            JSONObject remoteJMeterproperty = config.getJSONObject("remoteJMeterproperty");
            Set<Map.Entry<String, Object>> remoteEntry = remoteJMeterproperty.entrySet();
            for (Map.Entry<String, Object> entry1 : remoteEntry) {
                if (planParam.containsKey(entry1.getKey())) {
                    commandList.add("-G" + entry1.getKey() + "=" + planParam.getString(entry1.getKey()));
//                    commandBuilder.append("-G").append(entry1.getKey()).append("=").append(planParam.getString(entry1.getKey())).append(" ");
                } else {
//                    commandBuilder.append("-G").append(entry1.getKey()).append("=").append(entry1.getValue().toString()).append(" ");
                    commandList.add("-G" + entry1.getKey() + "=" + entry1.getValue().toString());
                }
            }
        }
        return commandList;
    }

    private void deleteFile(String folder) {
        File path = new File(folder);
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteFile(file.getAbsolutePath());
                    file.delete();
                }
            }
        }
    }

    public String getFileString(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            br.close();
            return buffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getFileString1(String filename) {
        try {
            BufferedReader br;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("gbk")));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            }
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            br.close();
            return buffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void updatePlanTaskDone(String planTaskId) {
        List<CaseResult> caseResultList = operationCasesResult.queryCasesResultByPlanIdAndState(planTaskId);
        if (caseResultList.size() == 0) {
            operationPlanTaskRecord.updatePlanTaskDoneState(planTaskId, "success", common.getUpdateTime(), "run");
        }
        boolean failCaseExist = false;
        if (caseResultList.size() > 0) {
            for (CaseResult object : caseResultList) {
                if (object.getState().equals("fail")) {
                    failCaseExist = true;
                    break;
                }
            }
        }
        if (failCaseExist) {
            operationPlanTaskRecord.updatePlanTaskDoneState(planTaskId, "fail", common.getUpdateTime(), "run");
        }
    }
}
