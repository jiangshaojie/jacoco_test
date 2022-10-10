package com.laiye.performance.dao;

import com.laiye.performance.enity.PlanTaskRecord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationPlanTaskRecord {
    @Insert("insert into plan_task_record (task_id,plan_id,update_time,state) values (#{taskId},#{planId},#{updateTime},#{state})")
    int insertPlanTaskRecord(PlanTaskRecord planTaskRecord);

    @Update("update plan_task_record set state=#{state},update_time=#{updateTime} where task_uuid=#{uuid}")
    int updateTaskState(String uuid, String state, String updateTime);

    @Update("update plan_task_record set state=#{state},update_time=#{updateTime} where task_id=#{planTaskId} and state=#{BeforeUpdateState}")
    int updatePlanTaskState(String planTaskId, String state, String updateTime, String BeforeUpdateState);

    @Update("update plan_task_record set state=#{state},execution_completion_time=#{completionTime} where task_id=#{planTaskId} and state=#{BeforeUpdateState}")
    int updatePlanTaskDoneState(String planTaskId, String state, String completionTime, String BeforeUpdateState);

    @Select("select * from plan_task_record where plan_id=#{planId} order by update_time desc")
    @Results(
            {@Result(id = true, column = "id", property = "id"),
                    @Result(column = "task_id", property = "taskId"),
                    @Result(column = "plan_id", property = "planId"),
                    @Result(column = "update_time", property = "updateTime"),
                    @Result(column = "state", property = "state"),
                    @Result(column = "execution_completion_time", property = "executionCompletionTime")}
    )
    List<PlanTaskRecord> queryPlanTaskRecordById(String planId);
}
