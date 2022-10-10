package com.laiye.performance.dao;

import com.laiye.performance.enity.CaseResult;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationCasesResultHistory {

    @Insert("insert into case_result_history (case_uuid,task_uuid,state,result_csv,result_log,overview_data,update_time,create_time,plan_task_id) " +
            "values (#{caseUuid},#{taskUuid},#{state},#{resultCsv},#{resultLog},#{overviewData},#{updateTime},#{createTime},#{planTaskId})")
    int insertCasesResult(CaseResult caseResult);

    @Update("update case_result_history set result_csv=#{resultCsv},result_log=#{resultLog},update_time=#{updateTime},state=#{state}," +
            "overview_data=#{overviewData}  where case_uuid=#{caseUuid} and task_uuid=#{taskUuid}")
    int updateCasesResult(CaseResult caseResult);

    @Update("update case_result_history set result_csv=#{resultCsv},result_log=#{resultLog},update_time=#{updateTime},state=#{state}," +
            "overview_data=#{overviewData},task_uuid=#{taskUuid} where case_uuid=#{caseUuid} and plan_task_id=#{planTaskId}")
    int updateCasesResultByCaseUuidAndPlanTaskId(CaseResult caseResult);

    @Delete("delete from case_result_history  where case_uuid=#{caseUuid} and plan_task_id=#{planTaskId}")
    int deleteCasesResultHistory(CaseResult caseResult);

    @Update("update case_result_history set result_csv=#{resultCsv},result_log=#{resultLog},update_time=#{updateTime},state=#{state}," +
            "overview_data=#{overviewData}  , create_time=#{createTime} where case_uuid=#{caseUuid} and task_uuid=#{taskUuid}")
    int updateCasesResultHistory(CaseResult caseResult);

    @Update("update case_result_history set update_time=#{updateTime},state=#{state} where task_uuid=#{taskUuid}")
    int updateCaseResultHistorySateUpdateTime(CaseResult caseResult);

    @Update("update case_result_history set update_time=#{updateTime}, create_time=#{createTime},state=#{state} where task_uuid=#{taskUuid}")
    int updateCaseResultHistorySateUpdateTimeAndCreateTime(CaseResult caseResult);

    @Select("select from case_result_history  where case_uuid=#{caseUuid} and plan_task_id=#{planTaskId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            /*@Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
            @Result(column = "result_csv", property = "resultCsv"),
            @Result(column = "result_log", property = "resultLog"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "overview_data", property = "overviewData"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")*/
    })
    CaseResult queryCasesResultHistoryByCaseUuidAndPlanTaskId(CaseResult caseResult);


    @Select("select * from case_result_history where case_uuid=#{caseUuid} order by id desc limit 1")
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
    CaseResult queryCasesResult(String name, String caseUuid);

    @Select("select * from case_result_history where task_uuid=#{taskId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "case_uuid", property = "caseUuid"),
            @Result(column = "task_uuid", property = "taskUuid"),
            @Result(column = "result_csv", property = "resultCsv"),
            @Result(column = "result_csv", property = "resultStringCsv"),
            @Result(column = "result_log", property = "resultLog"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "overview_data", property = "overviewData"),
            @Result(column = "state", property = "state"),
            @Result(column = "create_time", property = "createTime")
    })
    CaseResult queryCasesResultByTaskId(String taskId);

    @Select("select * from case_result_history where case_uuid=#{caseUuid} order by id desc")
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
    List<CaseResult> queryCasesResultList(String caseUuid);

    @Select("select * from case_result_history where plan_task_id=#{planTaskId} order by create_time desc")
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
    List<CaseResult> queryCasesResultByPlanTaskId(String planTaskId);

    @Select("select * from case_result_history where case_uuid=#{caseUuid} and state=#{state}")
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
    List<CaseResult> queryCasesResultByResultState(String caseUuid, String state);
}
