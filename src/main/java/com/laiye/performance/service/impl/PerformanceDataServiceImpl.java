package com.laiye.performance.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laiye.performance.component.ScheduledTask;
import com.laiye.performance.component.TaskManagement;
import com.laiye.performance.dao.*;
import com.laiye.performance.enity.*;
import com.laiye.performance.enums.ConstantFileType;
import com.laiye.performance.enums.Constants;
import com.laiye.performance.enums.TestDataTypes;
import com.laiye.performance.model.RespResult;
import com.laiye.performance.model.SampleData;
import com.laiye.performance.service.PerformanceDataService;
import com.laiye.performance.utils.OSSUtil;
import com.laiye.performance.utils.OperationFile;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class PerformanceDataServiceImpl implements PerformanceDataService {
    @Autowired
    OperationJmxFileDescription operationJmxFileDescription;
    @Autowired
    OperationProjectManagement operationProjectManagement;
    @Autowired
    OperationCases operationCases;
    @Autowired
    OperationCasePlan operationCasePlan;
    @Autowired
    OperationTestDataRecord operationTestDataRecord;
    @Autowired
    OperationCasesResult operationCasesResult;
    @Autowired
    OperationCasesResultHistory operationCasesResultHistory;
    @Autowired
    OperationFile operationFile;
    @Autowired
    OperationPlanTaskRecord operationPlanTaskRecord;
    @Autowired
    TaskManagement taskManagement;
    @Autowired
    OperationTaskState operationTaskState;
    @Autowired
    ScheduledTask scheduledTask;
    @Autowired
    OperationTag operationTag;

    @Override
    public void getJmxFile() {
        List<JmxFileDescription> jmxFileDescriptions = operationJmxFileDescription.getJmxFiles();
        System.out.printf(String.valueOf(jmxFileDescriptions.size()));
        System.out.println("******");
        System.out.printf(jmxFileDescriptions.get(0).toString());
    }

    @Override
    public RespResult uploadJmxFile(MultipartFile file, String projectName, String businessName, boolean isOverwritingUpload) {
        RespResult<JSONObject> result = new RespResult<>();
        JSONObject message = new JSONObject();
        result.setData(message);
        result.setCode(1);
        if (file.isEmpty()) {
            result.setCode(0);
            message.put("error", "file 为空");
            return result;
        }
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
        List<JmxFileDescription> existFile = operationJmxFileDescription.queryJmxFileByNameAndProjectName(file.getOriginalFilename(), projectManagement.getId());
        if (existFile.size() > 0 && (isOverwritingUpload == false)) {
            result.setCode(0);
            message.put("error", "file已存在");
            return result;
        }
        String filePath = "data/" + projectName;
        /*if (StringUtils.isEmpty(bussinessName)) {
            filePath = "data/" + projectName;
        } else {
            filePath = "data/" + projectName + "/" + bussinessName;
        }*/
        ProjectManagement projectManagement1 = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, "case");
        JSONObject jsonObjectParams = extractedJmxParams(file);
        JmxFileDescription jmxFileDescription = new JmxFileDescription();
        jmxFileDescription.setName(file.getOriginalFilename());
//        jmxFileDescription.setProjectName(projectName);
//        jmxFileDescription.setBusinessName(businessName);
        jmxFileDescription.setProjectNameId(projectManagement.getId());
        jmxFileDescription.setBusinessNameId(projectManagement1.getId());
        String uuid = getUuid();
        String updateTime = getUpdateTime();
        jmxFileDescription.setUuid(uuid);
        jmxFileDescription.setUpdateTime(updateTime);
        jmxFileDescription.setContent(jsonObjectParams.toJSONString());
        int insetRe = 0;
        if (existFile.size() > 0) {
            JmxFileDescription jmxFileDescription1 = existFile.get(0);
            jmxFileDescription1.setUpdateTime(updateTime);
            jmxFileDescription1.setContent(jsonObjectParams.toJSONString());
            insetRe = operationJmxFileDescription.updateJmxFiles(jmxFileDescription1); //覆盖写只更新时间
        } else {
            insetRe = operationJmxFileDescription.insertJmxFiles(jmxFileDescription);
        }

        if (insetRe == 0) {
            result.setCode(0);
            message.put("error", "数据库更新失败");
            return result;
        }
//        file.getInputStream();
        OSSUtil.uploadFile(projectName, ConstantFileType.JMX_FILE_TYPE.getValue(), file);
        String saveR = saveFile(file, filePath);
        if (saveR.equals("fail")) {
            result.setCode(0);
            message.put("error", "文件保存失败");
            return result;
        }
        result.setCode(1);
        message.put("success", "文件上传成功");
        return result;
    }

    private JSONObject extractedJmxParams(MultipartFile file) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        NodeList nl = document.getElementsByTagName("jmeterTestPlan");
        Node item = nl.item(0);
        List<String> list = new ArrayList<>();
        parseChildNode(item, list);
        log.info("jmx文件解析参数： {} {}", file.getOriginalFilename(), list);
        JSONObject jsonObject = ParseNodeString(list);
        log.info("jmx文件解析结果：{} {}", file.getOriginalFilename(), jsonObject);
        return jsonObject;
    }


    private void parseChildNode(Node node, List<String> list) {
//        List<String> list = new ArrayList<>();
        if (!StringUtils.isEmpty(node.getNodeValue())) {
            if (!StringUtils.isEmpty(node.getNodeValue().trim()) & node.getNodeValue().contains("__P")) {
                list.add(node.getNodeValue());
            }
        }
        NodeList childNodeList = node.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node nodeItem = childNodeList.item(i);
            parseChildNode(nodeItem, list);
        }
    }

    private JSONObject ParseNodeString(List<String> stringList) {
        Pattern pattern = Pattern.compile("^\\$\\{__P\\([\\S]*\\)\\}$");
        Pattern pattern1 = Pattern.compile("\\([\\S]*\\)");
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < stringList.size(); i++) {
            String item = stringList.get(i);
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                Matcher matcher1 = pattern1.matcher(item);
                matcher1.find();
                String subString = matcher1.group(0);
                String[] strings = subString.replace("(", "").replace(")", "").split(",");
                if (strings.length == 2) {
                    jsonObject.put(strings[0], strings[1]);
                } else {
                    jsonObject.put(strings[0], "");
                }
            }
        }
        return jsonObject;
    }

    private String getUuid() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    private String getUpdateTime() {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String updateTime = simpleDateFormat1.format(date);
        return updateTime;
    }


    @Override
    public RespResult insertProjectManagement(String name) {
        RespResult<JSONObject> respResult = new RespResult<>();
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(name);
        if (projectManagement != null) {
            jsonObject.put("message", "项目已存在");
            respResult.setCode(0);
            return respResult;
        }
        projectManagement = new ProjectManagement();
        projectManagement.setName(name);
        projectManagement.setBusinessName("");
        projectManagement.setCategory("project");
        int result = operationProjectManagement.insertProjectManagement(projectManagement);
        if (result != 1) {
            jsonObject.put("message", "添加失败");
            respResult.setCode(1);
            return respResult;
        }
        projectManagement.setBusinessName("ALL");
        projectManagement.setCategory("case");
        result = operationProjectManagement.insertProjectManagement(projectManagement);
        if (result != 1) {
            jsonObject.put("message", "添加失败");
            respResult.setCode(1);
            return respResult;
        }
        projectManagement.setCategory("plan");
        result = operationProjectManagement.insertProjectManagement(projectManagement);
        if (result != 1) {
            jsonObject.put("message", "添加失败");
            respResult.setCode(1);
            return respResult;
        }
        jsonObject.put("message", "success");
        return respResult;
    }

/*    @Override
    public RespResult queryManagement(ProjectManagement projectManagement) {
        return null;
    }*/

    @Override
    public ProjectManagement queryProjectManagementById(String id) {
        return null;
    }

    @Override
    public RespResult addCases(String caseName, String jmxUuid, String projectName, String businessName, JSONObject config, JSONArray tags) {
        RespResult respResult = new RespResult<>();
        JSONObject message = new JSONObject();
        respResult.setData(message);
        respResult.setCode(1);
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
        ProjectManagement projectManagement1 = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, "case");
        Cases cases = new Cases();
        cases.setConfig(JSONObject.toJSONString(config));
        cases.setJmxUuid(jmxUuid);
        cases.setName(caseName);
        String time = getUpdateTime();
        cases.setUpdateTime(time);
        cases.setCreateTime(time);
        cases.setUuid(getUuid());
//        cases.setProjectName(projectName);
//        cases.setBusinessName(businessName);
        cases.setProjectNameId(projectManagement.getId());
        cases.setBusinessNameId(projectManagement1.getId());
        cases.setTagId(tags.toJSONString());
        int insertR = operationCases.insertCases(cases);
        if (insertR == 0) {
            respResult.setCode(0);
            message.put("message", "fail");
            return respResult;
        }
        message.put("message", "success");
        return respResult;
    }

    @Override
    public RespResult updateCases(String caseName, String caseUuid, String jmxUuid, JSONObject config) {
        RespResult respResult = new RespResult<>();
        JSONObject message = new JSONObject();
        respResult.setData(message);
        respResult.setCode(1);
        Cases cases = new Cases();
        cases.setConfig(JSONObject.toJSONString(config));
        cases.setJmxUuid(jmxUuid);
        cases.setName(caseName);
        cases.setUpdateTime(getUpdateTime());
        cases.setUuid(caseUuid);
        int insertR = operationCases.updateCases(cases);
        if (insertR == 0) {
            respResult.setCode(0);
            message.put("message", "fail");
            return respResult;
        }
        message.put("message", "success");
        return respResult;
    }

    @Override
    public RespResult addPlan(String name, String projectName, String businessName, JSONArray casesIds, JSONObject planParam) {
        RespResult result = new RespResult<>();
        result.setCode(1);
        String time = getUpdateTime();
        JSONObject jsonObject = new JSONObject();
        result.setData(jsonObject);
        CasePlan casePlan = new CasePlan();
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
        ProjectManagement projectManagement1 = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, "plan");
        casePlan.setName(name);
        casePlan.setProjectNameId(projectManagement.getId());
        casePlan.setBusinessNameId(projectManagement1.getId());
        casePlan.setCasesIds(casesIds.toJSONString());
        casePlan.setCreateTime(time);
        casePlan.setUpdateTime(time);
        casePlan.setParams(JSONObject.toJSONString(planParam));
        int insertRe = operationCasePlan.insertCasePlan(casePlan);
        if (insertRe == 1) {
            jsonObject.put("message", "success");
        } else {
            jsonObject.put("message", "fail");
            result.setData(0);
            return result;
        }
        return result;
    }

    private String saveFile(MultipartFile file, String filePath) {
//        String filename = file.getOriginalFilename(); //获取上传文件原来的名称
//        String filePath = "/Users/laoniu/temp/";
        File temp = new File(filePath);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        File destFile = new File(temp + "/" + file.getOriginalFilename());
        try {
            destFile.createNewFile();
            file.transferTo(Paths.get(destFile.getPath())); //把上传的文件保存至本地。可接受文件夹名称或者file 对象，本处使用的文件夹
            log.info(file.getOriginalFilename() + " 上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }
        return "ok";
    }

    @Override
    public String queryCases(String projectName, String businessName) {
        RespResult result = new RespResult();
        result.setCode(0);
        JSONObject message = new JSONObject();
        result.setData(message);
//        JSONObject jsonObject = new JSONObject();
//        List<String> keys = config.keySet();
        List<Cases> cases;
        if (businessName.equals("ALL")) {
            ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
            cases = operationCases.queryCasesByProjectName(projectManagement.getId());
        } else {
            ProjectManagement projectManagement = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, "case");
            cases = operationCases.queryCasesByProjectNameAndBusinessName(projectManagement.getId());
        }
        JSONArray caseJsonArray = getCasesDetail(cases);
        message.put("cases", caseJsonArray);
        return JSONObject.toJSONString(result);
    }

    private JSONArray getCasesDetail(List<Cases> cases) {
        HashMap<String, JSONObject> caseResultHashMap = new HashMap<>();
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < cases.size(); i++) {
            uuids.add(cases.get(i).getUuid());
        }
        List<CaseResult> caseResultList = new ArrayList<>();
        if (uuids.size() > 0) {
            caseResultList = operationCasesResult.queryCasesResultByUuid(uuids);
        }
        for (int i = 0; i < caseResultList.size(); i++) {
            CaseResult caseResult = caseResultList.get(i);
            JSONObject caseResultJson = new JSONObject();

            caseResultJson.put("createTime", caseResult.getCreateTime());
            caseResultJson.put("state", caseResult.getState());
            if (caseResult.getState().equals("success")) {
                JSONArray newOverJsonData = getNewOverJsonData(caseResult);
                caseResultJson.put("overviewData", newOverJsonData);
            } /*else {
                caseResultJson.put("overviewData", caseResult.getState());
            }*/
            caseResultHashMap.put(caseResult.getCaseUuid(), caseResultJson);
        }
        JSONArray caseJsonArray = new JSONArray();
        for (int i = 0; i < cases.size(); i++) {
            Cases cases1 = cases.get(i);
            JSONObject jsonObject1 = JSONObject.parseObject(JSONObject.toJSONString(cases1));
            JSONObject jsonObject2 = caseResultHashMap.get(cases1.getUuid());
            if (jsonObject2 != null) {
                try {
//                    jsonObject1.put("overviewData", caseResultHashMap.get(cases1.getUuid()).getJSONObject("overviewData"));
                    jsonObject1.put("overviewData", caseResultHashMap.get(cases1.getUuid()).getJSONArray("overviewData"));
                } catch (Exception e) {
                    log.info("overviewData 解析失败");
//                    jsonObject1.put("overviewData", caseResultHashMap.get(cases1.getUuid()).getString("overviewData"));
                }
                jsonObject1.put("executeTime", caseResultHashMap.get(cases1.getUuid()).getString("createTime"));
                jsonObject1.put("state", caseResultHashMap.get(cases1.getUuid()).getString("state"));
            }
            caseJsonArray.add(jsonObject1);
        }
        return caseJsonArray;
    }

    private JSONArray getNewOverJsonData(CaseResult caseResult) {
        JSONObject config = operationFile.getResourcesJosonconfig("config/jmeterResultConfig.json");
        JSONObject overJsonData = JSONObject.parseObject(caseResult.getOverviewData());
//                JSONObject newOverJsonData = new JSONObject();
        JSONArray newOverJsonData = new JSONArray();
        for (Map.Entry<String, Object> entry : overJsonData.entrySet()) {
            if (!entry.getKey().equals("Total")) {
                JSONObject singleData = JSONObject.parseObject(entry.getValue().toString(), SampleData.class).getJsonData();
                JSONObject filteredData = new JSONObject();
                for (Map.Entry<String, Object> entry1 : singleData.entrySet()) {
                    if (config.containsKey(entry1.getKey())) {
                        filteredData.put(entry1.getKey(), entry1.getValue());
                    }
                }
//                        newOverJsonData.put(entry.getKey(), filteredData);
                newOverJsonData.add(filteredData);
            }
        }
        return newOverJsonData;
    }

    @Override
    public RespResult queryPlan(String projectName, String businessName) {
        RespResult result = new RespResult();
        result.setCode(0);
        JSONObject jsonObject = new JSONObject();
        result.setData(jsonObject);
        List<CasePlan> casePlans;
        if (businessName.equals("ALL")) {
            ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
            casePlans = operationCasePlan.queryCasePlanByProject(projectManagement.getId());
        } else {
            ProjectManagement projectManagement = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, "plan");
            casePlans = operationCasePlan.queryCasePlanBusinessNameId(projectManagement.getId());
        }
        if (casePlans.size() == 0) {
            jsonObject.put("casePlans", casePlans);
            return result;
        }
        Set<Object> caseIds = new HashSet<>();
        for (int i = 0; i < casePlans.size(); i++) {
            caseIds.addAll(JSONArray.parseArray(casePlans.get(i).getCasesIds()));
        }
        List<Cases> casesInId = operationCases.queryCasesByUuids(caseIds);
        HashMap<String, String> casesHashMap = new HashMap<>();
        for (int i = 0; i < casesInId.size(); i++) {
            casesHashMap.put(casesInId.get(i).getUuid(), casesInId.get(i).getName());
        }
        JSONArray jsonArray = null;

        for (int i = 0; i < casePlans.size(); i++) {
            CasePlan casePlan = casePlans.get(i);
            jsonArray = JSONArray.parseArray(casePlan.getCasesIds());
            casePlan.setCasesIds("");
            for (Object object : jsonArray) {
                casePlan.setCasesIds(casePlan.getCasesIds() + casesHashMap.get(object) + ";");
//                caseNames = caseNames + casesHashMap.get(object) + ";";
            }
            casePlan.setCasesIds(casePlan.getCasesIds().substring(0, casePlan.getCasesIds().length() - 1));
        }
        jsonObject.put("casePlans", casePlans);
        return result;
    }

    @Override
    public RespResult queryProject() {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(0);
        result.setData(jsonObject);
        List<ProjectManagement> projectManagements = operationProjectManagement.queryProject();
        HashSet projects = new HashSet();
        HashMap<String, Object> projectBusinessName = new HashMap<>();
       /* JSONObject caseJson = new JSONObject();
        JSONObject planJson = new JSONObject();*/
        HashMap<String, List> caseMap = new HashMap<>();
        HashMap<String, List> planMap = new HashMap<>();
        projectBusinessName.put("cases", caseMap);
        projectBusinessName.put("plans", planMap);
        for (ProjectManagement projectManagement : projectManagements) {
            projects.add(projectManagement.getName());
            caseMap.put(projectManagement.getName(), new ArrayList<String>());
            planMap.put(projectManagement.getName(), new ArrayList<String>());
        }
        List<ProjectManagement> projectManagementList = operationProjectManagement.queryCaseTab();
        for (ProjectManagement projectManagement : projectManagementList) {
            caseMap.get(projectManagement.getName()).add(projectManagement.getBusinessName());
        }
        List<ProjectManagement> projectManagementList1 = operationProjectManagement.queryPlanTab();
        for (ProjectManagement projectManagement : projectManagementList1) {
            planMap.get(projectManagement.getName()).add(projectManagement.getBusinessName());
        }
        jsonObject.put("projects", projects);
        jsonObject.put("projectBusinessName", projectBusinessName);
        return result;
    }

    @Override
    public RespResult queryJmxFile(String name, String businessName) {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(0);
        result.setData(jsonObject);
        List<JmxFileDescription> jmxFileDescriptions;
        if (businessName.equals("ALL")) {
            jmxFileDescriptions = operationJmxFileDescription.queryJmxFileByProjectName(name);
        } else {
            jmxFileDescriptions = operationJmxFileDescription.queryJmxFileByProjectNameAndBusinessName(name, businessName);
        }
        jsonObject.put("jmxFiles", jmxFileDescriptions);
        return result;
    }

    @Override
    public RespResult uploadTestData(MultipartFile[] folder, String name, String bussinessName) {
        RespResult<JSONObject> result = new RespResult<>();
        JSONObject message = new JSONObject();
        result.setData(message);
        result.setCode(1);
        if (folder.length == 0) {
            result.setCode(0);
            message.put("error", "folder 为空");
            return result;
        }
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(name);
        checkDataIsExist(folder, projectManagement.getId(), true);
        String filePath = "data/" + name;
        /*JmxFileDescription jmxFileDescription = new JmxFileDescription();
        jmxFileDescription.setName(file.getOriginalFilename());
        jmxFileDescription.setProjectName(projectName);
        jmxFileDescription.setBusinessName(bussinessName);
        String uuid = getUuid();
        String updateTime = getUpdateTime();
        jmxFileDescription.setUuid(uuid);
        jmxFileDescription.setUpdateTime(updateTime);
        int insetRe = operationJmxFileDescription.insertJmxFiles(jmxFileDescription);
        if (insetRe == 0) {
            result.setCode(0);
            message.put("error", "数据库更新失败");
            return result;
        }*/
//        TestDataRecord testDataRecord = convertTestDataRecord(folder, name);
        OSSUtil.uploadFile(name, ConstantFileType.TEST_DATA_TYPE.getValue(), folder);
        String saveR = saveMultiFile(filePath, folder);
        if (saveR.equals("fail")) {
            result.setCode(0);
            message.put("error", "文件夹为空");
            return result;
        }
        result.setCode(1);
        message.put("success", "文件夹传成功");
        return result;
    }

   /* private TestDataRecord convertTestDataRecord(MultipartFile[] folder, String name) {
        String fileName = folder[0].getOriginalFilename();
        TestDataRecord testDataRecord = new TestDataRecord();
        if (fileName.contains("/")) {
            String folderName = fileName.split("/")[0];
            testDataRecord.setProjectName(name);
            testDataRecord.setDataName(folderName);
            testDataRecord.setType(TestDataTypes.FOLDER.getValue());
        }
        if (!fileName.contains("/")) {
            testDataRecord.setProjectName(name);
            testDataRecord.setDataName(fileName);
            testDataRecord.setType(TestDataTypes.FILE.getValue());
        }
        return testDataRecord;
    }*/

    @Override
    public RespResult queryTestData(String projectName) {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(1);
        result.setData(jsonObject);
        List<TestDataRecord> operationTestDataRecords = operationTestDataRecord.queryTestDataRecordList(projectName);
        jsonObject.put("testDatas", operationTestDataRecords);
        return result;
    }

    @Override
    public RespResult queryCaseResultHistory(String caseUuid) {

        RespResult result = new RespResult();
        result.setCode(0);
        JSONObject resultJson = new JSONObject();
        result.setData(resultJson);
        Cases cases = operationCases.queryCasesById(caseUuid);
        String caseName = cases.getName();
        JmxFileDescription jmxFileDescription = operationJmxFileDescription.queryJmxFileByUuid(cases.getJmxUuid());
        String jmxName = jmxFileDescription.getName();
        List<CaseResult> caseResultHistory = operationCasesResultHistory.queryCasesResultList(cases.getUuid());
        List<JSONObject> list = new ArrayList<>();
        JSONObject config = operationFile.getResourcesJosonconfig("config/jmeterResultConfig.json");
        for (int i = 0; i < caseResultHistory.size(); i++) {
            CaseResult caseResult = caseResultHistory.get(i);
            if (!caseResult.getState().equals(Constants.TASK_STATE_SUCCESS.getValue())) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", caseName);
            jsonObject.put("jmxName", jmxName);
            jsonObject.put("executeTime", caseResult.getCreateTime());
            jsonObject.put("taskId", caseResult.getTaskUuid());
            /*JSONObject overJsonData = JSONObject.parseObject(caseResult.getOverviewData());
            for (Map.Entry<String, Object> entry : overJsonData.entrySet()) {
                overJsonData.put(entry.getKey(), JSONObject.parseObject(entry.getValue().toString(), SampleData.class).getJsonData());
            }*/
            JSONObject overJsonData = JSONObject.parseObject(caseResult.getOverviewData());
//            JSONObject newOverJsonData = new JSONObject();
            JSONArray newOverJsonData = new JSONArray();
            if (overJsonData != null) {
                for (Map.Entry<String, Object> entry : overJsonData.entrySet()) {
                    if (!entry.getKey().equals("Total")) {
                        JSONObject singleData = JSONObject.parseObject(entry.getValue().toString(), SampleData.class).getJsonData();
                        JSONObject filteredData = new JSONObject();
                        for (Map.Entry<String, Object> entry1 : singleData.entrySet()) {
                            if (config.containsKey(entry1.getKey())) {
                                filteredData.put(entry1.getKey(), entry1.getValue());
                            }
                        }
//                        newOverJsonData.put(entry.getKey(), filteredData);
                        newOverJsonData.add(filteredData);
                    }
                }
            }
//            caseResultJson.put("overviewData", newOverJsonData);
            jsonObject.put("overviewData", newOverJsonData);
            list.add(jsonObject);
        }
        resultJson.put("caseHistory", list);
        return result;
    }

    @Override
    public RespResult deleteJmxFile(String jmxUuid) {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setData(jsonObject);
        result.setCode(1);
        return result;
    }

    @Override
    public RespResult queryPlanCases(String planId) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        CasePlan casePlan = operationCasePlan.queryCasePlanById(planId);
        String caseIds = casePlan.getCasesIds();
        List<String> caseIdsList = JSONArray.parseArray(caseIds, String.class);
        Set<String> casesIdSet = new HashSet<>();
        casesIdSet.addAll(caseIdsList);
        List<Cases> cases = operationCases.queryCasesByUuids(casesIdSet);
        Set<String> jmxUuids = new HashSet<>();
        for (int i = 0; i < cases.size(); i++) {
            jmxUuids.add(cases.get(i).getJmxUuid());
        }
        List<JmxFileDescription> jmxFileDescriptionList = operationJmxFileDescription.queryJmxFileByUuidList(jmxUuids);
        HashMap<String, String> uuidAndJmxName = new HashMap<>();
        for (int i = 0; i < jmxFileDescriptionList.size(); i++) {
            uuidAndJmxName.put(jmxFileDescriptionList.get(i).getUuid(), jmxFileDescriptionList.get(i).getName());
        }
        Cases cases1;
        for (int i = 0; i < cases.size(); i++) {
            cases1 = cases.get(i);
            cases1.setJmxUuid(uuidAndJmxName.get(cases1.getJmxUuid()));
        }
        JSONArray casesDetailArray = getCasesDetail(cases);
        jsonObject.put("cases", casesDetailArray);
        return respResult;
    }

    @Override
    public RespResult addPane(String projectName, String businessName, String category) {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(1);
        result.setData(jsonObject);
        ProjectManagement projectManagement = new ProjectManagement();
        projectManagement.setName(projectName);
        projectManagement.setBusinessName(businessName);
        projectManagement.setCategory(category);
        ProjectManagement exist = operationProjectManagement.queryByNameAndBusinessNameAndCategory(projectName, businessName, category);
        if (exist != null) {
            result.setCode(0);
            jsonObject.put("message", "已存在");
            return result;
        }
        int re = operationProjectManagement.insertProjectManagement(projectManagement);
        if (re == 0) {
            result.setCode(0);
            jsonObject.put("message", "添加失败");
            return result;
        }
        jsonObject.put("message", "添加成功");
        return result;
    }

    @Override
    public RespResult queryProjectName() {
        RespResult result = new RespResult();
        JSONObject jsonObject = new JSONObject();
        result.setCode(1);
        result.setData(jsonObject);
        List<ProjectManagement> projectManagementList = operationProjectManagement.queryProjectName();
        List<String> projectNames = new ArrayList<>();
        for (int i = 0; i < projectManagementList.size(); i++) {
            projectNames.add(projectManagementList.get(i).getName());
        }
        jsonObject.put("projectNames", projectNames);
        return result;
    }

    @Override
    public RespResult queryCaseLog(String uuid) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        CaseResult caseResult = operationCasesResult.querySingleCasesResultByUuid(uuid);
        if (caseResult == null) {
            respResult.setCode(0);
            jsonObject.put("message", "日志不存在");
        }
        byte[] unZipLog = caseResult.getResultLog();
//        String log = operationFile.unzipString(unZipLog);
        String log = null;
        try {
            log = operationFile.uncompress(unZipLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        jsonObject.put("message", log);
        return respResult;
    }

    @Override
    public RespResult queryCaseHistoryLog(String taskId) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        CaseResult caseResult = operationCasesResultHistory.queryCasesResultByTaskId(taskId);
        if (caseResult == null) {
            respResult.setCode(0);
            jsonObject.put("message", "日志不存在");
        }
        byte[] unZipLog = caseResult.getResultLog();
//        String log = operationFile.unzipString(unZipLog);
        String log = null;
        try {
            log = operationFile.uncompress(unZipLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        jsonObject.put("message", log);
        return respResult;
    }

    @Override
    public RespResult queryPlanHistory(String planId) {
        RespResult respResult = new RespResult();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        List<PlanTaskRecord> planTaskRecords = operationPlanTaskRecord.queryPlanTaskRecordById(planId);
        jsonObject.put("planTaskRecords", planTaskRecords);
        return respResult;
    }

    @Override
    public RespResult queryPlanCaseResult(String planId, String planTaskId) {
        RespResult respResult = new RespResult();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        CasePlan casePlan = operationCasePlan.queryCasePlanById(planId);
        List caseIds = JSONObject.parseArray(casePlan.getCasesIds());
        List<CaseResult> caseResultList = operationCasesResultHistory.queryCasesResultByPlanTaskId(planTaskId);
        Set<String> casesIdSet = new HashSet<>();
        Set<String> casesIdSetWithResult = new HashSet<>();
        casesIdSet.addAll(caseIds);
//        HashMap<String, JSONObject> caseALL = new HashMap<>();
        for (int i = 0; i < caseResultList.size(); i++) {
//            caseWithResult.put(caseResultList.get(i).getCaseUuid(),null);
            casesIdSetWithResult.add(caseResultList.get(i).getCaseUuid());
            casesIdSet.add(caseResultList.get(i).getCaseUuid());
        }
        List<String> caseIdWithNoResult = new ArrayList<>();
        for (String object : casesIdSet) {
            if (!casesIdSetWithResult.contains(object)) {
                caseIdWithNoResult.add(object);
            }
        }
        List<Cases> cases = operationCases.queryCasesByUuids(casesIdSet);
        HashMap<String, Cases> uuidAndCase = new HashMap<>();
//        HashMap<String, String> uuidAndCase = new HashMap<>();
        Set<String> jmxUuids = new HashSet<>();
        for (int i = 0; i < cases.size(); i++) {
            Cases case1 = cases.get(i);
            jmxUuids.add(case1.getJmxUuid());
            uuidAndCase.put(case1.getUuid(), case1);
        }
        List<JmxFileDescription> jmxFileDescriptionList = operationJmxFileDescription.queryJmxFileByUuidList(jmxUuids);
        HashMap<String, String> uuidAndJmxName = new HashMap<>();
        for (int i = 0; i < jmxFileDescriptionList.size(); i++) {
            uuidAndJmxName.put(jmxFileDescriptionList.get(i).getUuid(), jmxFileDescriptionList.get(i).getName());
        }
        HashMap<String, String> caseUuidAndJmxName = new HashMap<>();
        for (int i = 0; i < cases.size(); i++) {
            Cases case1 = cases.get(i);
            caseUuidAndJmxName.put(case1.getUuid(), uuidAndJmxName.get(case1.getJmxUuid()));
        }
        JSONArray jsonArray = new JSONArray();
        String caseIdTemp;

        //填充，当前plan_task_id有关联结果的用例
        for (int i = 0; i < caseResultList.size(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            CaseResult caseResult = caseResultList.get(i);
            jsonObject1.put("caseUuid", caseResult.getCaseUuid());
            jsonObject1.put("caseName", uuidAndCase.get(caseResult.getCaseUuid()).getName());
            jsonObject1.put("id", caseResult.getId());
            jsonObject1.put("state", caseResult.getState());
            if (caseResult.getState().equals("success")) {
                jsonObject1.put("overviewData", getNewOverJsonData(caseResult));
            }
//            jsonObject1.put("overviewData", JSONObject.parseObject(caseResult.getOverviewData()));
            jsonObject1.put("taskUuid", caseResult.getTaskUuid());
            jsonObject1.put("updateTime", caseResult.getUpdateTime());
            jsonObject1.put("createTime", caseResult.getCreateTime());
            jsonObject1.put("jmxFileName", caseUuidAndJmxName.get(caseResult.getCaseUuid()));
            jsonObject1.put("config", uuidAndCase.get(caseResult.getCaseUuid()).getConfig());
            jsonArray.add(jsonObject1);
        }
        //填充，当前plan_task_id没有关联结果的用例
        for (int i = 0; i < caseIdWithNoResult.size(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            caseIdTemp = caseIdWithNoResult.get(i);
            Cases cases1 = uuidAndCase.get(caseIdTemp);
            jsonObject1.put("caseUuid", caseIdTemp);
            jsonObject1.put("caseName", cases1.getName());
            jsonObject1.put("id", cases1.getId());
            jsonObject1.put("updateTime", cases1.getUpdateTime());
            jsonObject1.put("createTime", cases1.getCreateTime());
            jsonObject1.put("jmxFileName", caseUuidAndJmxName.get(cases1.getUuid()));
            jsonObject1.put("config", uuidAndCase.get(cases1.getUuid()).getConfig());
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("caseResultList", jsonArray);
        return respResult;
    }

    @Override
    public RespResult queryPlanCasesAndOptionalCase(String planId) {
        RespResult respResult = new RespResult();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        CasePlan casePlan = operationCasePlan.queryCasePlanById(planId);
        if (casePlan == null) {
            respResult.setCode(0);
            jsonObject.put("message", "plan不存在");
            return respResult;
        }
        List<Cases> cases;
//        String businessName = casePlan.getBusinessName();
        String projectNameId = casePlan.getProjectNameId();
        /*if (businessName.equals("ALL")) {
            cases = operationCases.queryCasesByProjectName(projectName);
        } else {
            cases = operationCases.queryCasesByProjectNameAndBusinessName(projectName, businessName);
        }*/
        cases = operationCases.queryCasesByProjectName(projectNameId);

        jsonObject.put("optionalCase", cases);
        jsonObject.put("planCases", JSONArray.parse(casePlan.getCasesIds()));
        JSONObject planParam = new JSONObject();
        try {
            planParam = JSONObject.parseObject(casePlan.getParams());

        } catch (Exception e) {
            log.info("plan 参数解析失败： {}", planId);
        }
        List<JSONObject> params = new ArrayList<>();
        if (planParam != null) {
            for (Map.Entry<String, Object> entry : planParam.entrySet()
            ) {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("key", entry.getKey());
                jsonObject1.put("value", entry.getValue());
                params.add(jsonObject1);
            }
        } else {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("key", "");
            jsonObject1.put("value", "");
            params.add(jsonObject1);
        }
        jsonObject.put("planParam", params);
        return respResult;
    }

    @Override
    public RespResult updatePlan(String planName, String planId, JSONArray casesIds, JSONObject planParam) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        CasePlan casePlan = new CasePlan();
        casePlan.setId(planId);
        casePlan.setCasesIds(JSONObject.toJSONString(casesIds));
        casePlan.setParams(JSONObject.toJSONString(planParam));
        casePlan.setName(planName);
        casePlan.setUpdateTime(getUpdateTime());
        int re = operationCasePlan.updateCasePlan(casePlan);
        if (re == 0) {
            jsonObject.put("message", "更新失败");
            respResult.setCode(0);
            return respResult;
        }
        jsonObject.put("message", "success");
        return respResult;
    }

    @Override
    public RespResult updateCase(String name, String caseUuid, JSONObject config, String jmxUuid, JSONArray tags) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        Cases cases = new Cases();
        cases.setName(name);
        cases.setUuid(caseUuid);
        cases.setConfig(JSONObject.toJSONString(config));
        cases.setUpdateTime(getUpdateTime());
        cases.setJmxUuid(jmxUuid);
        cases.setTagId(tags.toJSONString());
        int re = 0;
        if (StringUtils.isEmpty(jmxUuid)) {
            re = operationCases.updateCases(cases);
        } else {
            re = operationCases.updateCases1(cases);
        }
        if (re == 0) {
            jsonObject.put("message", "更新失败");
            respResult.setData(0);
            return respResult;
        }
        jsonObject.put("message", "更新成功");
        return respResult;
    }

    @Override
    public XSSFWorkbook planPerformanceResultDownLoad(String planId, String planTaskId) {
        CasePlan casePlan = operationCasePlan.queryCasePlanById(planId);
//        List caseIds = JSONObject.parseArray(casePlan.getCasesIds());
        Set caseIds = JSONObject.parseObject(casePlan.getCasesIds(), Set.class);
        log.info(caseIds.getClass().getName());
        List<CaseResult> caseResultList = operationCasesResultHistory.queryCasesResultByPlanTaskId(planTaskId);
        List<Cases> cases = operationCases.queryCasesByUuids(caseIds);
        HashMap<String, Cases> uuidAndCase = new HashMap<>();
        for (int i = 0; i < cases.size(); i++) {
            Cases case1 = cases.get(i);
            uuidAndCase.put(case1.getUuid(), case1);
        }
        JSONArray jsonArray = new JSONArray();
        String caseIdTemp;
        //填充，当前plan_task_id有关联结果的用例
        for (int i = 0; i < caseResultList.size(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            CaseResult caseResult = caseResultList.get(i);
            jsonObject1.put("caseName", uuidAndCase.get(caseResult.getCaseUuid()).getName());
            if (caseResult.getState().equals("success")) {
                jsonObject1.put("overviewData", getNewOverJsonData(caseResult));
            }
            try {
                JSONObject configJson = JSONObject.parseObject(uuidAndCase.get(caseResult.getCaseUuid()).getConfig());
                String threadNum = configJson.getJSONObject("localParam").getString("number_threads");
                jsonObject1.put("threadNum", threadNum);
            } catch (Exception exception) {
                log.info("用例无config: {}", caseResult.getCaseUuid());
                jsonObject1.put("threadNum", "");
            }
//            jsonObject1.put("config", uuidAndCase.get(caseResult.getCaseUuid()).getConfig());
            jsonArray.add(jsonObject1);
        }
        XSSFWorkbook workbook = writeExcel(jsonArray, "sheet1");
        return workbook;
    }

    @Override
    public RespResult preEditCheckCase(String caseUuid) {
        RespResult respResult = new RespResult<>();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        List<CaseResult> caseResultList = operationCasesResultHistory.queryCasesResultByResultState(caseUuid, "success");
        if (caseResultList.size() > 0) {
            jsonObject.put("isHasSuccessHistory", true);
            return respResult;
        }
        jsonObject.put("isHasSuccessHistory", false);
        return respResult;
    }

    @Override
    public RespResult checkJmxFileIsExist(String fileName, String projectName, String businessName) {
        RespResult respResult = new RespResult<>();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        ProjectManagement projectManagement = operationProjectManagement.queryProjectByName(projectName);
        List<JmxFileDescription> existFile = operationJmxFileDescription.queryJmxFileByNameAndProjectName(fileName, projectManagement.getId());
        if (existFile.size() > 0) {
            jsonObject.put("isExist", true);
            return respResult;
        }
        jsonObject.put("isExist", false);
        return respResult;
    }

    @Override
    public RespResult copyCase(String caseUuid) {
        RespResult respResult = new RespResult<>();
        respResult.setCode(1);
        JSONObject jsonObject = new JSONObject();
        respResult.setData(jsonObject);
        Cases cases = operationCases.queryCasesById(caseUuid);
        String uuid = getUuid();
        String caseName = cases.getName() + uuid.substring(uuid.length() - 5);
        cases.setName(caseName);
        String updateTime = getUpdateTime();
        cases.setCreateTime(updateTime);
        cases.setUpdateTime(updateTime);
        cases.setUuid(uuid);
        int re = operationCases.insertCases(cases);
        if (re == 0) {
            respResult.setCode(0);
            jsonObject.put("error", "用例复制失败");
            return respResult;
        }
        jsonObject.put("success", "用例复制成功");
        return respResult;
    }

    @Override
    public File downloadJmxFile(String projectName, String jmxFileName) {
        String rootDir = System.getProperty("user.dir");
        String fileName = rootDir + "/data/" + projectName + "/" + jmxFileName;
        File file = new File(fileName);
        return file;
    }

    @Override
    public RespResult abortTask(String taskId) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
//        CaseResult caseResult = operationCasesResult.queryCasesResultByTaskId(taskId);
        int re = checkOrUpdateTaskState(taskId, true); //终止状态
        if (re == 2) {
            taskManagement.killProcess(taskId);
        }
        jsonObject.put("message", "任务已终止: " + taskId);
        return respResult;
    }

    @Override
    public RespResult queryTask() {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        List<CaseResult> caseResultList = operationCasesResult.queryStateWaitOrRun();
        if (caseResultList.size() == 0) {
            jsonObject.put("taskData", new JSONArray());
            return respResult;
        }
        Set<String> caseUuidList = new HashSet<>();
        for (int i = 0; i < caseResultList.size(); i++) {
            caseUuidList.add(caseResultList.get(i).getCaseUuid());
        }
        List<Cases> casesList = operationCases.queryCasesByUuids(caseUuidList);
        HashMap<String, Cases> casesHashMap = new HashMap<>();
        for (int i = 0; i < casesList.size(); i++) {
            Cases cases = casesList.get(i);
            casesHashMap.put(cases.getUuid(), cases);
        }
        List<ProjectManagement> projectManagementList = operationProjectManagement.queryProject();
        HashMap<String, String> projectHashMap = new HashMap<>();
        for (ProjectManagement projectManagement : projectManagementList) {
            projectHashMap.put(projectManagement.getId(), projectManagement.getName());
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < caseResultList.size(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            CaseResult caseResult = caseResultList.get(i);
            jsonObject1.put("taskUuid", caseResult.getTaskUuid());
            jsonObject1.put("updateTime", caseResult.getUpdateTime());
            jsonObject1.put("state", caseResult.getState());
            jsonObject1.put("name", casesHashMap.get(caseResult.getCaseUuid()).getName());
            jsonObject1.put("projectName", projectHashMap.get(casesHashMap.get(caseResult.getCaseUuid()).getProjectNameId()));
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("taskData", jsonArray);
        return respResult;
    }

    @Override
    public RespResult manualSyncData() {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        scheduledTask.scheduledTaskSyncData();
        return respResult;
    }

    @Override
    public RespResult addTag(String name, String projectName, String tabName, String category) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        int re = operationTag.insertTag(name, projectName, tabName, category);
        if (re < 0) {
            respResult.setCode(0);
            jsonObject.put("message", "添加标签失败");
            return respResult;
        }
        jsonObject.put("message", "添加标签成功");
        return respResult;
    }

    @Override
    public RespResult queryTag(String projectName, String tabName, String category) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        List<Tag> re = operationTag.queryTag(projectName, tabName, category);
        List tags = new ArrayList();
        for (Tag tag : re
        ) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("label", tag.getName());
            jsonObject1.put("value", tag.getId());
            tags.add(jsonObject1);
        }
        jsonObject.put("tags", tags);
        return respResult;
    }

    @Override
    public RespResult deleteTag(String id) {
        RespResult respResult = new RespResult();
        JSONObject jsonObject = new JSONObject();
        respResult.setCode(1);
        respResult.setData(jsonObject);
        int re = operationTag.deleteTag(id);
        if (re == 0) {
            respResult.setCode(0);
            jsonObject.put("message", "标签删除失败");
            return respResult;
        }
        jsonObject.put("message", "标签删除成功");
        return respResult;
    }

    @Synchronized
    public int checkOrUpdateTaskState(String taskId, Boolean isModifyState) {
        //如果修改状态前wait，终止状态返回0，
        //如果修改状态前run，终止状态返回2，
        //不修改，只查询是否为wait，返回为1
        CaseResult caseResult = operationCasesResult.queryCasesResultByTaskId(taskId);
        log.info("checkOrUpdateTaskState check task taskState {} {}:", taskId, caseResult.getState());
        String updateTime = getUpdateTime();
        if (caseResult.getState().equals(Constants.TASK_STATE_WAIT.getValue()) && isModifyState) {
            caseResult.setCreateTime(updateTime);
            caseResult.setUpdateTime(updateTime);
            caseResult.setState(Constants.TASK_STATE_ABORT.getValue());
            operationCasesResult.updateCaseResultSateUpdateTimeAndCreateTime(caseResult);
            operationCasesResultHistory.updateCaseResultHistorySateUpdateTimeAndCreateTime(caseResult);
            operationTaskState.updateTaskState(taskId, Constants.TASK_STATE_ABORT.getValue(), updateTime);
            return 0;
        }
        if (caseResult.getState().equals(Constants.TASK_STATE_RUN.getValue()) && isModifyState) {
            caseResult.setUpdateTime(updateTime);
            caseResult.setState(Constants.TASK_STATE_ABORT.getValue());
            operationCasesResult.updateCaseResultSateUpdateTime(caseResult);
            operationCasesResultHistory.updateCaseResultHistorySateUpdateTime(caseResult);
            operationTaskState.updateTaskState(taskId, Constants.TASK_STATE_ABORT.getValue(), updateTime);
            return 2;
        }
        if (caseResult.getState().equals(Constants.TASK_STATE_WAIT.getValue()) && isModifyState == false) {
            return 1;
        }
        return -1;
    }

    private XSSFWorkbook writeExcel(JSONArray jsonArray, String sheetName) {
//        Workbook workbook = null;
//        FileOutputStream outputStream = null;
     /*   String filePath = "demo.xlsx";
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/
//      workbook = new XSSFWorkbook(inputStream);
        XSSFWorkbook workbook = new XSSFWorkbook();
       /* XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        /*int index = workbook.getSheetIndex(sheetName);
        if (index > -1) {
            workbook.removeSheetAt(workbook.getSheetIndex(sheetName));
        }*/
        workbook = createHeader(workbook, sheetName);
//      Sheet sheet = workbook.cloneSheet(workbook.getSheetIndex("模板"));
//      workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);
        Sheet sheet = workbook.getSheet(sheetName);
        int rowNum = 1;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String caseName = jsonObject.getString("caseName");
            String threadNum = jsonObject.getString("threadNum");
            JSONArray performanceData = jsonObject.getJSONArray("overviewData");
            for (Object object : performanceData) {
                JSONObject jsonObject1 = (JSONObject) object;
                Row row = sheet.createRow(rowNum);
//                    CellAddress cellAddress = new CellAddress();
//                    sheet.setActiveCell();
//            row.createCell(0).setCellValue(fileName);
                row.createCell(2).setCellValue(threadNum);
                row.createCell(3).setCellValue(jsonObject1.getString("pct1ResTime"));
                row.createCell(4).setCellValue(jsonObject1.getString("pct2ResTime"));
                row.createCell(5).setCellValue(jsonObject1.getString("pct3ResTime"));
                row.createCell(6).setCellValue(jsonObject1.getString("throughput"));
                row.createCell(7).setCellValue(jsonObject1.getString("errorPct"));
                row.createCell(8).setCellValue(jsonObject1.getString("transaction"));
                row.createCell(9).setCellValue(caseName);
                rowNum = rowNum + 1;
            }


        }
           /* File file = new File(URI.create());
            outputStream = new FileOutputStream(file);
            workbook.write(outputStream);*/
        return workbook;
    }

    private XSSFWorkbook createHeader(XSSFWorkbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 15 * 256);
        sheet.setColumnWidth(5, 15 * 256);
        sheet.setColumnWidth(6, 15 * 256);
        sheet.setColumnWidth(7, 15 * 256);
        sheet.setColumnWidth(8, 15 * 256);
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        Row row0 = sheet.createRow(0);
//        row0.createCell(0).setCellValue(value);
//        Row row1 = sheet.createRow(1);
        row0.createCell(0).setCellValue("机器");
        row0.createCell(1).setCellValue("场景");
        row0.createCell(2).setCellValue("并发数");
        row0.createCell(3).setCellValue("90%响应时间");
        row0.createCell(4).setCellValue("95%响应时间");
        row0.createCell(5).setCellValue("99%响应时间");
        row0.createCell(6).setCellValue("TPS");
        row0.createCell(7).setCellValue("错误率");
        row0.createCell(8).setCellValue("线程组名字");
        row0.createCell(9).setCellValue("执行用例名字");
        return workbook;
    }

    @Synchronized
    private int checkDataIsExist(MultipartFile[] folder, String name, boolean update) {
        String fileName = folder[0].getOriginalFilename();
        if (fileName.contains("/")) {
            String folderName = fileName.split("/")[0];
            TestDataRecord testDataRecord = operationTestDataRecord.queryTestDataRecord(name, folderName, TestDataTypes.FOLDER.getValue());
            if (testDataRecord == null) {
                operationTestDataRecord.insertTestDataRecord(folderName, TestDataTypes.FOLDER.getValue(), name, getUpdateTime());
            } else {
                if (update) {
                    operationTestDataRecord.updateTestDataRecord(testDataRecord.getId(), getUpdateTime());
                }
            }
            return 0;
        }
        if (!fileName.contains("/")) {
            TestDataRecord testDataRecord = operationTestDataRecord.queryTestDataRecord(name, fileName, TestDataTypes.FILE.getValue());
            if (testDataRecord == null) {
                operationTestDataRecord.insertTestDataRecord(fileName, TestDataTypes.FILE.getValue(), name, getUpdateTime());
            } else {
                if (update) {
                    operationTestDataRecord.updateTestDataRecord(testDataRecord.getId(), getUpdateTime());
                }
            }
            return 1;
        }
        return 2;
    }

    public String saveMultiFile(String basePath, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return "fail";
        }
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        for (MultipartFile file : files) {
            log.info("file OriginalFilename: {}", file.getOriginalFilename());
            String filePath = basePath + "/" + file.getOriginalFilename();
            makeDir(filePath);
            File dest = new File(filePath);
            try {
                dest.createNewFile();
                file.transferTo(Paths.get(dest.getPath()));
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 确保目录存在，不存在则创建   * @param filePath
     */
    private void makeDir(String filePath) {
        if (filePath.lastIndexOf('/') > 0) {
            String dirPath = filePath.substring(0, filePath.lastIndexOf('/'));
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
}
