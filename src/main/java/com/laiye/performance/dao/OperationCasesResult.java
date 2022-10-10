package com.laiye.performance.dao;

import com.laiye.performance.enity.CaseResult;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationCasesResult {

    @Insert("insert into case_result (case_uuid,task_uuid,state,result_csv,result_log,overview_data,update_time,create_time,plan_task_id) " +
            "values (#{caseUuid},#{taskUuid},#{state},#{resultCsv},#{resultLog},#{overviewData},#{updateTime},#{createTime},#{planTaskId})")
    int insertCasesResult(CaseResult caseResult);

    @Update("update case_result set result_csv=#{resultCsv},result_log=#{resultLog},update_time=#{updateTime},state=#{state}," +
            "overview_data=#{overviewData}  , create_time=#{createTime} where case_uuid=#{caseUuid} and task_uuid=#{taskUuid}")
    int updateCasesResult(CaseResult caseResult);

    @Select("select * from case_result where task_uuid==#{taskId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid")
    })
    CaseResult queryCasesResultByTaskId(String taskId);

    @Update("update case_result set update_time=#{updateTime},state=#{state} where task_uuid=#{taskUuid}")
    int updateCaseResultSateUpdateTime(CaseResult caseResult);

    @Update("update case_result set update_time=#{updateTime}, create_time=#{createTime},state=#{state} where task_uuid=#{taskUuid}")
    int updateCaseResultSateUpdateTimeAndCreateTime(CaseResult caseResult);

    @Select("select * from case_result where case_uuid=#{caseUuid} order by id desc limit 1")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
//            @Result(column = "result_csv", property = "resultCsv"),
//            @Result(column = "result_log", property = "resultLog"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "overview_data", property = "overviewData"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")
    })
    CaseResult queryCasesResult(String name, String caseUuid);

    @Select({"<script>  select * from case_result where case_uuid in",
            "<foreach collection='caseUuids' item='uuid' open='(' separator=',' close=')'>",
            "#{uuid}",
            "</foreach>",
            "</script>"})
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
//            @Result(column = "result_csv", property = "resultCsv"),
//            @Result(column = "result_log", property = "resultLog"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "overview_data", property = "overviewData"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")
    })
    List<CaseResult> queryCasesResultByUuid(List<String> caseUuids);

    @Delete("delete  from case_result where case_uuid=#{caseUuid}")
    int deleteCaseResult(String caseUuid);

    @Select("select * from case_result where case_uuid=#{uuid}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
            @Result(column = "result_csv", property = "resultCsv"),
            @Result(column = "result_log", property = "resultLog"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "overview_data", property = "overviewData"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")
    })
    CaseResult querySingleCasesResultByUuid(String uuid);

    @Select("select * from case_result where plan_task_id=#{planTaskId} and (state='run' or state='fail')")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")
    })
    List<CaseResult> queryCasesResultByPlanIdAndState(String planTaskId);

    @Select("select * from case_result where state='run' or state='wait' order by update_time desc")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "state", property = "state"),
    })
    List<CaseResult> queryStateWaitOrRun();
}
