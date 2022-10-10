package com.laiye.performance.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationTaskState {
    @Insert("insert into taskstate (task_uuid,state,update_time) values (#{uuid},#{state},#{updateTime})")
    int insertTaskState(String uuid, String state, String updateTime);

    @Update("update taskstate set state=#{state},update_time=#{updateTime} where task_uuid=#{uuid}")
    int updateTaskState(String uuid, String state, String updateTime);
}
