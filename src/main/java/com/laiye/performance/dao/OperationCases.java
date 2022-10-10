package com.laiye.performance.dao;

import com.laiye.performance.enity.Cases;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OperationCases {

    @Insert("insert into cases (name,jmx_uuid,config,uuid,project_name_id,business_name_id,update_time,create_time) " +
            "values (#{name},#{jmxUuid},#{config},#{uuid},#{projectNameId},#{businessNameId},#{updateTime},#{createTime})")
    int insertCases(Cases cases);

    @Update("update cases set name=#{name}, config=#{config},update_time=#{updateTime},tag_id=#{tagId} where uuid=#{uuid}")
    int updateCases(Cases cases);

    @Select("select * from cases where uuid==#{uuid}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime")
    })
    Cases queryCasesById(String uuid);

    @Select({"<script>  select * from cases where uuid in",
            "<foreach collection='uuids' item='uuid' open='(' separator=',' close=')'>",
            "#{uuid}",
            "</foreach>",
            "</script>"})
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "project_name_id", property = "projectNameId")
    })
    List<Cases> queryCasesByUuids(Set uuids);

    @Select("select a.id,a.name name, b.name jmx_uuid, a.config, a.uuid, a.update_time, a.project_name_id, a.business_name_id,a.create_time,a.tag_id" +
            "  from cases as a," +
            "  jmx_file_description as b" +
            "  where a.project_name_id = #{projectNameId}" +
            "  and a.jmx_uuid = b.uuid")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "tag_id", property = "tagId"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
    })
    List<Cases> queryCasesByProjectName(String projectNameId);

    @Select("select a.id,a.name name, b.name jmx_uuid, a.config, a.uuid, a.update_time, a.project_name_id, a.business_name_id,a.create_time,a.tag_id" +
            "  from cases as a," +
            "  jmx_file_description as b" +
            "  where a.business_name_id = #{businessNameId}" +
            "  and a.jmx_uuid = b.uuid")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "tag_id", property = "tagId"),
    })
    List<Cases> queryCasesByProjectNameAndBusinessName(String businessNameId);

    @Update("update cases set name=#{name}, config=#{config},update_time=#{updateTime},jmx_uuid=#{jmxUuid},tag_id=#{tagId} where uuid=#{uuid}")
    int updateCases1(Cases cases);

    @Update("update cases set business_name_id=#{businessNameId}, project_name_id=#{projectNameId} where id=#{id}")
    void updateCasesBusinessName(String id, String projectNameId, String businessNameId);
}
