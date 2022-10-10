package com.laiye.performance.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TaskManagement {
    private ConcurrentHashMap<String, Process> concurrentHashMap = new ConcurrentHashMap();

    public void addTaskIdProcess(String taskId, Process process) {
        concurrentHashMap.put(taskId, process);
    }

    public int killProcess(String taskId) {
        Process process1 = concurrentHashMap.get(taskId);
        concurrentHashMap.remove(taskId);
        int re = 0;
        if (process1 == null) {
            log.info("process is null before manual destroyProcess: {}", taskId);
            return re;
        }
        if (process1.isAlive()) {
            //判断process是否退出，若退出则摧毁
            process1.destroy();
            log.info("manual destroyProcess: {}", taskId);
            re = 1;
        } else {
            log.info("process is not alive before manual destroyProcess : {}", taskId);
        }
        return re;
    }

    public void deleteTaskIdProcess(String taskId) {
        concurrentHashMap.remove(taskId);
    }
}
