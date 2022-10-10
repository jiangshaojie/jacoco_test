package com.laiye.performance.component;

import com.laiye.performance.dao.OperationProjectManagement;
import com.laiye.performance.dao.OperationTestDataRecord;
import com.laiye.performance.enity.ProjectManagement;
import com.laiye.performance.enity.TestDataRecord;
import com.laiye.performance.utils.OSSUtil;
import com.laiye.performance.utils.OperationFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class ScheduledTask {
    @Autowired
    OperationTestDataRecord operationTestDataRecord;
    @Autowired
    OperationFile operationFile;
    @Autowired
    OperationProjectManagement operationProjectManagement;

    @Scheduled(cron = "0 0 9 ? * *")
    public void scheduledTaskSyncData() {
        log.info("数据同步任务执行时间：{}", LocalDateTime.now());
        List<TestDataRecord> testDataRecordList = operationTestDataRecord.queryAllTestDataRecordList();
        List<ProjectManagement> projectManagementList = operationProjectManagement.queryProject();
        HashMap<String, String> projectMap = new HashMap<>();
        for (ProjectManagement projectManagement : projectManagementList) {
            projectMap.put(projectManagement.getId(), projectManagement.getName());
        }
        for (TestDataRecord testDataRecord : testDataRecordList) {
            String filePath = "data/" + projectMap.get(testDataRecord.getProjectNameId()) + "/" + testDataRecord.getDataName();
            if (!operationFile.isFileExist(filePath)) {
                log.info("文件：{} 不存在，开始下载", filePath);
                OSSUtil.downloadFolder(filePath, filePath);
            }
        }
        log.info("数据同步任务执行完成时间：{}", LocalDateTime.now());
    }
}
