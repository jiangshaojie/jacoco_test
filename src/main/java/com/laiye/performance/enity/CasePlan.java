package com.laiye.performance.enity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.lang.reflect.Field;

@Data
public class CasePlan {
    private String id;
    private String name;
    private String casesIds;
    private String createTime;
    private String updateTime;
    private String params;
    private String projectNameId;
    private String businessNameId;

    private JSONObject convertToJson() {
        Class casePlan = this.getClass();
        Field[] fields = casePlan.getDeclaredFields();
        JSONObject jsonObject = new JSONObject();
        for (Field field : fields) {
            try {
                jsonObject.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
