package com.laiye.performance.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OperationProcess {
    @Async("TaskExecutor")
    public void destroyProcess(Process process, long minute, String taskId) {
        int count;
        if (minute * 60 % 5 > 0) {
            count = (int) (minute * 60 / 5) + 1;
        } else {
            count = (int) (minute * 60 / 5);
        }
        try {
            log.info("destroyProcess start sleep, {}  {}", taskId, minute);
            for (int i = 0; i < count; i++) {
                TimeUnit.SECONDS.sleep(5);
                if (!process.isAlive()) { //判断process是否退出
                    break;
                }
            }
            if (process.isAlive()) {
                //判断process是否退出，若退出则摧毁
                process.destroy();
                log.info("process is exist,destroyProcess: {}", taskId);
            } else {
                log.info("process execute done, timer is done: {}", taskId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Async("TaskExecutor")
    public void bufferReaderLog(BufferedReader bufferedReader, String taskUuid, Process process, String logType) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.info("{}: {} {}", logType, taskUuid, line);
            if (line.equals("errorlevel=1")) {
                log.info("errorLevel destroy: {}", taskUuid);
                process.destroy();
            }
        }
    }
}
