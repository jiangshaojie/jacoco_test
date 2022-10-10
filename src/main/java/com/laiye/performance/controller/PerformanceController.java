package com.laiye.performance.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laiye.performance.enity.ProjectManagement;
import com.laiye.performance.model.RespResult;
import com.laiye.performance.service.PerformanceDataService;
import com.laiye.performance.service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController()
@RequestMapping("/data")
public class PerformanceController {
    @Autowired
    PerformanceDataService performanceDataService;
    @Autowired
    PerformanceService performanceService;

    @PostMapping("/getjmxfiles")
    public void getJmxFiles() {
        performanceDataService.getJmxFile();
    }

    @PostMapping("/edit/case")
    public String editCase(@RequestBody String param) {
        log.info("editCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String name = req.getString("name");
        String caseUuid = req.getString("uuid");
        String jmxUuid = (String) req.getOrDefault("jmxUuid", "");
        JSONObject config = req.getJSONObject("config");
        JSONArray tags = req.getJSONArray("tags");
        RespResult result = performanceDataService.updateCase(name, caseUuid, config, jmxUuid, tags);
        log.info("editCase resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/uploadjmxfile")
    public String uploadJmxFile(@RequestParam("file") MultipartFile file, @RequestParam("projectName") String projectName, @RequestParam("businessName") String businessName, @RequestParam("isOverwritingUpload") boolean isOverwritingUpload) {
        log.info("uploadJmxFile req: {}, {}", projectName, businessName, isOverwritingUpload);
        RespResult result = performanceDataService.uploadJmxFile(file, projectName, businessName, isOverwritingUpload);
        log.info("uploadJmxFile resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/check/jmx-file-exist")
    public String checkJmxFileIsExist(@RequestBody String param) {
        log.info("checkJmxFileIsExist req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String projectName = req.getString("projectName");
        String businessName = req.getString("businessName");
        String fileName = req.getString("fileName");
        RespResult result = performanceDataService.checkJmxFileIsExist(fileName, projectName, businessName);
        log.info("checkJmxFileIsExist resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/delete-jmxfile")
    public String deleteJmxFile(@RequestBody String param) {
        log.info("deleteJmxFile req: {}", param);
        String jmxUuid = JSONObject.parseObject(param).getString("uuid");
        RespResult result = performanceDataService.deleteJmxFile(jmxUuid);
        log.info("deleteJmxFile resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/upload-test-data")
    public String uploadTestData(@RequestParam("file") MultipartFile[] folder, @RequestParam("name") String name, @RequestParam("businessName") String bussinessName) {
        log.info("uploadTestData req: {}, {}", name, bussinessName);
        RespResult result = performanceDataService.uploadTestData(folder, name, bussinessName);
        log.info("uploadTestData resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query/jmxfile")
    public String queryJmxFile(@RequestBody String param) {
        log.info("queryJmxFile req: {}", param);
        JSONObject jsonObject = JSONObject.parseObject(param);
        String projectName = jsonObject.getString("projectName");
        String businessName = jsonObject.getString("businessName");
        RespResult result = performanceDataService.queryJmxFile(projectName, businessName);
        log.info("queryJmxFile resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query/testdata")
    public String queryTestData(@RequestBody String param) {
        log.info("queryTestData req: {}", param);
        JSONObject jsonObject = JSONObject.parseObject(param);
        String projectName = jsonObject.getString("projectName");
        RespResult result = performanceDataService.queryTestData(projectName);
        log.info("queryTestData resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/add-project")
    public String addProject(@RequestBody String param) {
        log.info("addProject req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String name = req.getString("name");
       /* String businessName = req.getString("businessName");
        ProjectManagement projectManagement = new ProjectManagement();
        projectManagement.setName(name);
        projectManagement.setBusinessName(businessName);*/
        RespResult result = performanceDataService.insertProjectManagement(name);
        log.info("addProject result: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/queryproject")
    public String queryProject() {
        RespResult result = performanceDataService.queryProject();
        log.info("queryProject result: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/addcase")
    public String addCase(@RequestBody String param) {
        log.info("addCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String jmxUuid = req.getString("jmxUuid");
        String caseName = req.getString("caseName");
        JSONObject config = req.getJSONObject("config");
        String projectName = req.getString("projectName");
        String businessName = req.getString("businessName");
        JSONArray tags = req.getJSONArray("tags");
        RespResult result = performanceDataService.addCases(caseName, jmxUuid, projectName, businessName, config, tags);
        log.info("addCase resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/addplan")
    public String addPlan(@RequestBody String param) {
        log.info("addPlan req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        JSONArray casesIds = req.getJSONArray("casesId");
        String name = req.getString("name");
        String projectName = req.getString("projectName");
        String businessName = req.getString("businessName");
        JSONObject planParam = req.getJSONObject("planParam");
        RespResult result = performanceDataService.addPlan(name, projectName, businessName, casesIds, planParam);
        log.info("addPlan resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/update-plan")
    public String updatePlan(@RequestBody String param) {
        log.info("updatePlan req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        JSONArray casesIds = req.getJSONArray("casesId");
        String planId = req.getString("planId");
        String planName = req.getString("planName");
        JSONObject planParam = req.getJSONObject("planParam");
        RespResult result = performanceDataService.updatePlan(planName, planId, casesIds, planParam);
        log.info("updatePlan resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query/plan")
    public String queryPlan(@RequestBody String param) {
        log.info("queryPlan req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String projectName = req.getString("projectName");
        String businessName = req.getString("businessName");
        RespResult result = performanceDataService.queryPlan(projectName, businessName);
        log.info("queryPlan resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query-plan-cases")
    public String queryPlanCases(@RequestBody String param) {
        log.info("queryPlanCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String planId = req.getString("planId");
        RespResult result = performanceDataService.queryPlanCases(planId);
        log.info("queryPlanCase resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query-plan-history-case-result")
    public String queryPlanCaseResult(@RequestBody String param) {
        log.info("queryPlanCaseResult req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String planTaskId = req.getString("planTaskId");
        String planId = req.getString("planId");
        RespResult result = performanceDataService.queryPlanCaseResult(planId, planTaskId);
        log.info("queryPlanCaseResult resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query-plan-history")
    public String queryPlanHistory(@RequestBody String param) {
        log.info("queryPlanHistory req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String planId = req.getString("planId");
        RespResult result = performanceDataService.queryPlanHistory(planId);
        log.info("queryPlanHistory resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/updatecase")
    public String updateCase(@RequestBody String param) {
        log.info("updateCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String jmxUuid = req.getString("jmxUuid");
        String caseName = req.getString("caseName");
        JSONObject config = req.getJSONObject("config");
        String caseUuid = req.getString("caseUuid");
        RespResult result = performanceDataService.updateCases(caseName, caseUuid, jmxUuid, config);
        log.info("updateCase resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/executecase")
    public String executeCase(@RequestBody String param) {
        log.info("executeCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
       /* String jmxUuid = req.getString("jmxUuid");
        String caseName = req.getString("caseName");
        JSONObject config = req.getJSONObject("config");
        String caseUuid = req.getString("caseUuid");*/
        String caseUuid = req.getString("caseUuid");
        String planTaskId = req.getString("planTaskId");
        String planId = (String) req.getOrDefault("planId", "");
        RespResult result = performanceService.executeTask(caseUuid, planTaskId, planId);
        log.info("executeCase resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/executecaseplan")
    public String executeCasePlan(@RequestBody String param) {
        log.info("executeCasePlan req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String id = req.getString("planId");
        String projectName = req.getString("projectName");
        RespResult result = performanceService.executeCasePlan(id, projectName);
        log.info("executeCasePlan resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/querycaseresult")
    public String queryCaseResult(@RequestBody String param) {
        log.info("queryCaseResult req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String caseUuid = req.getString("caseUuid");
        String name = req.getString("name");
        RespResult result = performanceService.queryCaseResult(name, caseUuid);
//        log.info("queryCaseResult resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/case-result-history")
    public String queryCaseResultHistory(@RequestBody String param) {
        log.info("queryCaseResult req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String caseUuid = req.getString("caseUuid");
        RespResult result = performanceDataService.queryCaseResultHistory(caseUuid);
        log.info("queryCaseResult resp: {}", JSONObject.toJSON(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query/native-report")
    public String queryNativeReport(@RequestBody String param) {
        log.info("queryNativeReport req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String taskId = req.getString("taskId");
        RespResult result = performanceService.queryNativeReport(taskId);
        log.info("queryNativeReport resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query/cases")
    public String queryCases(@RequestBody String param) {
        log.info("queryCases req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String businessName = req.getString("businessName");
        String projectName = req.getString("projectName");
        String result = performanceDataService.queryCases(projectName, businessName);
//        log.info("queryCases resp: {}", JSONObject.toJSON(result));
        return result;
    }

    @PostMapping("/add-pane")
    public String addPane(@RequestBody String param) {
        log.info("addPane req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String businessName = req.getString("businessName");
        String projectName = req.getString("projectName");
        String category = req.getString("category");
        RespResult result = performanceDataService.addPane(projectName, businessName, category);
        log.info("addPane resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query-project-name")
    public String queryProjectName() {
        RespResult result = performanceDataService.queryProjectName();
        log.info("queryProjectName resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/query-case-log")
    public String queryCaseLog(@RequestBody String param) {
        log.info("queryCaseLog req: {}", param);
        String uuid = JSONObject.parseObject(param).getString("uuid");
        RespResult result = performanceDataService.queryCaseLog(uuid);
        log.info("queryCaseLog resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/case-history-log")
    public String queryCaseHistoryLog(@RequestBody String param) {
        log.info("queryCaseHistoryLog req: {}", param);
        String taskId = JSONObject.parseObject(param).getString("taskId");
        RespResult result = performanceDataService.queryCaseHistoryLog(taskId);
        log.info("queryCaseHistoryLog resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/plan/containCase-and-optionalCase")
    public String queryPlanContainCaseAndOptionalCase(@RequestBody String param) {
        log.info("queryPlanContainCaseAndOptionalCase req: {}", param);
        String planId = JSONObject.parseObject(param).getString("planId");
        RespResult result = performanceDataService.queryPlanCasesAndOptionalCase(planId);
        log.info("queryPlanContainCaseAndOptionalCase resp: {}", JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/plan/download/result")
    public void planResultDownload(HttpServletResponse response, @RequestBody String param) {
        log.info("planResultDownload req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String planTaskId = req.getString("planTaskId");
        String planId = req.getString("planId");
        XSSFWorkbook workbook = performanceDataService.planPerformanceResultDownLoad(planId, planTaskId);
        response.reset();
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
//        response.setContentLength((int) w);
        response.setHeader("Content-Disposition", "attachment;filename=result.xlsx");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        try {
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        return;
    }

    @PostMapping("/jmx-file/download")
    public void downloadJmxFile(HttpServletResponse response, @RequestBody String param) {
        log.info("downloadJmxFile req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String projectName = req.getString("projectName");
        String jmxFileName = req.getString("jmxFileName");
        File jmxFile = performanceDataService.downloadJmxFile(projectName, jmxFileName);
        response.reset();
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
//        response.setContentLength((int) w);
        response.setHeader("Content-Disposition", "attachment;filename=result.jmx");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(jmxFile));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.info("下载失败: {}", e);
        }
    }

    @PostMapping("/edit/pre-check-case")
    public String preEditCheckCase(@RequestBody String param) {
        log.info("preEditCheckCase: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String caseUuid = req.getString("caseUuid");
        RespResult respResult = performanceDataService.preEditCheckCase(caseUuid);
        log.info("preEditCheckCase resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/task/abort-task")
    public String abortTask(@RequestBody String param) {
        log.info("abortTask req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String taskId = req.getString("taskId");
        RespResult respResult = performanceDataService.abortTask(taskId);
        log.info("abortTask resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/task/query-task")
    public String queryTask() {
        RespResult respResult = performanceDataService.queryTask();
        log.info("queryTask resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/case/copy")
    public String copyCase(@RequestBody String param) {
        log.info("copyCase req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String caseUuid = req.getString("caseUuid");
        RespResult respResult = performanceDataService.copyCase(caseUuid);
        log.info("copyCase resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/manual/sync-data")
    public String manualSyncData() {
        RespResult respResult = performanceDataService.manualSyncData();
        log.info("manualSyncData resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/tag/add-tag")
    public String addTag(@RequestBody String param) {
        log.info("addTag req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String projectName = req.getString("projectName");
        String tabName = req.getString("tabName");
        String name = req.getString("name");
        String category = req.getString("category");
        RespResult respResult = performanceDataService.addTag(name, projectName, tabName, category);
        log.info("addTag resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/tag/query-tag")
    public String queryTag(@RequestBody String param) {
        log.info("queryTag req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String projectName = req.getString("projectName");
        String tabName = req.getString("tabName");
        String category = req.getString("category");
        RespResult respResult = performanceDataService.queryTag(projectName, tabName, category);
        log.info("queryTag resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    @PostMapping("/tag/delete-tag")
    public String deleteTag(@RequestBody String param) {
        log.info("deleteTag req: {}", param);
        JSONObject req = JSONObject.parseObject(param);
        String id = req.getString("id");
        RespResult respResult = performanceDataService.deleteTag(id);
        log.info("deleteTag resp: {}", JSONObject.toJSONString(respResult));
        return JSONObject.toJSONString(respResult);
    }

    /* @RequestMapping("/download")
     public String fileDownLoad(HttpServletResponse response, @RequestBody String param){
         String uuid = JSONObject.parseObject(param).getString("uuid");
         RespResult result = performanceDataService.queryCaseLog(uuid);
         String log = ((JSONObject) result.getData()).getString("message");
         response.reset();
         response.setContentType("application/octet-stream");
         response.setCharacterEncoding("utf-8");
         response.setContentLength((int) log.length());
         response.setHeader("Content-Disposition", "attachment;filename=" + fileName );
 //        log.info("queryCaseLog resp: {}", JSONObject.toJSONString(result));
         *//*File file = new File(downloadFilePath +'/'+ fileName);
        if(!file.exists()){
            return "下载文件不存在";
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName );

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            return "下载失败";
        }*//*
        return "下载成功";
    }*/
    @PostMapping("/test")
    public String test() {
        return "666";
    }
}
