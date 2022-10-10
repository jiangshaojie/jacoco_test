package com.laiye.performance.dao;

import com.laiye.performance.enity.TestDataRecord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationTestDataRecord {
    @Insert("insert into  test_data_record (data_name ,type,project_name_id,update_time) values(#{dataName},#{type},#{projectNameId},#{updateTime})")
    int insertTestDataRecord(String dataName, String type, String projectNameId, String updateTime);

    @Insert("update test_data_record set update_time=#{updateTime} where id=#{id}")
    int updateTestDataRecord(String id, String updateTime);

    @Select("select * from test_data_record where project_name_id=" +
            "(select id from project_management where name=#{projectName} and category='project')")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "data_name", property = "dataName"),
            @Result(column = "type", property = "type"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<TestDataRecord> queryTestDataRecordList(String projectName);

    @Select("select * from test_data_record")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "data_name", property = "dataName"),
            @Result(column = "type", property = "type"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "update_time", property = "updateTime")
    })
    List<TestDataRecord> queryAllTestDataRecordList();

    @Select("select * from test_data_record where data_name=#{dataName} and type=#{type}" +
            "and project_name_id=#{projectNameId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "data_name", property = "dataName"),
            @Result(column = "type", property = "type"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "update_time", property = "updateTime")
    })
    TestDataRecord queryTestDataRecord(String projectNameId, String dataName, String type);

}
