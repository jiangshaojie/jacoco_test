package com.laiye.performance.dao;

import com.laiye.performance.enity.CasePlan;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationCasePlan {

    @Insert("insert into case_plan (name,project_name_id,business_name_id,cases_ids,create_time,update_time,params) " +
            "values (#{name},#{projectNameId},#{businessNameId},#{casesIds},#{createTime},#{updateTime},#{params})")
    int insertCasePlan(CasePlan casePlan);

    @Update("update case_plan set cases_ids=#{casesIds},name=#{name},params=#{params},update_time=#{updateTime} where id=#{id}")
    int updateCasePlan(CasePlan casePlan);

    @Update("update case_plan set project_name_id=#{projectNameId},business_name_id=#{businessNameId} where id=#{id}")
    int updateCasePlan1(String id, String projectNameId, String businessNameId);

    @Select("select * from case_plan  where id=#{planId} and " +
            "project_name_id=(select id from project_management where name=#{projectName} and category='project')")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "params", property = "params"),
            @Result(column = "cases_ids", property = "casesIds")
    })
    CasePlan queryCasePlan(String planId, String projectName);

    @Select("select * from case_plan where id=#{planId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "params", property = "params"),
            @Result(column = "cases_ids", property = "casesIds")
    })
    CasePlan queryCasePlanById(String planId);

    @Select("select * from case_plan where business_name_id=#{businessNameId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "cases_ids", property = "casesIds"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<CasePlan> queryCasePlanBusinessNameId(String businessNameId);

    @Select("select * from case_plan where project_name_id=#{projectNameId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cases_ids", property = "casesIds"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId")
    })
    List<CasePlan> queryCasePlanByProject(String projectNameId);
}
