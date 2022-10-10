package com.laiye.performance.service;

import com.alibaba.fastjson.JSONObject;
import com.laiye.performance.enity.Cases;
import com.laiye.performance.model.RespResult;

import java.util.jar.JarEntry;

public interface TaskService {
    void executeJmeterTask(String caseUuid, String rootDir, String taskUuid, String planTaskId, JSONObject planParam);


}
