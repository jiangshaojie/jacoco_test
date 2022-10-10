package com.laiye.performance.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Common {
    public String getUpdateTime() {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String updateTime = simpleDateFormat1.format(date);
        return updateTime;
    }
}
