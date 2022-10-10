package com.laiye.performance.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.awt.geom.QuadCurve2D;
import java.lang.reflect.Field;
import java.text.DecimalFormat;

@Data
public class SampleData {
    private String transaction;
    private Integer sampleCount;
    private Integer errorCount;
    private Float errorPct;
    private Float meanResTime;
    private Float minResTime;
    private Float maxResTime;
    private Float pct1ResTime;
    private Float pct2ResTime;
    private Float pct3ResTime;
    private Float throughput;
    private Float receivedKBytesPerSec;
    private Float sentKBytesPerSec;

    public JSONObject getJsonData() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        Class sampleData = this.getClass();
        Field[] fields = sampleData.getDeclaredFields();
        JSONObject jsonObject = new JSONObject();
        for (Field field : fields) {
            try {
                if (field.getType().equals(Float.class)) {
                    jsonObject.put(field.getName(), decimalFormat.format(field.get(this)));
                } else {
                    jsonObject.put(field.getName(), field.get(this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
