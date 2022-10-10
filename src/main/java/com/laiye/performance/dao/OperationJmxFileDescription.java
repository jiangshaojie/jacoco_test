package com.laiye.performance.dao;

import com.laiye.performance.enity.JmxFileDescription;
import org.apache.ibatis.annotations.*;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OperationJmxFileDescription {
    @Select("select * from jmx_file_description")
    List<JmxFileDescription> getJmxFiles();

    @Insert("insert into jmx_file_description (name,uuid,project_name_id,business_name_id,update_time,content) " +
            "values (#{name},#{uuid},#{projectNameId},#{businessNameId},#{updateTime},#{content})")
    int insertJmxFiles(JmxFileDescription jmxFileDescription);


    @Select("select * from jmx_file_description where name=#{name} and project_name_id=#{projectNameId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    List<JmxFileDescription> queryJmxFileByNameAndProjectName(String name, String projectNameId);

    @Select("select * from jmx_file_description where uuid=#{uuid}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    JmxFileDescription queryJmxFileByUuid(String uuid);

    @Select("select * from jmx_file_description where project_name_id=" +
            "(select id from project_management where name=#{projectName} and category='project') and " +
            "business_name_id=(select id from project_management where name=#{projectName} and business_name=#{businessName} and category='case')")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    List<JmxFileDescription> queryJmxFileByProjectNameAndBusinessName(String projectName, String businessName);

    @Select("select * from jmx_file_description where project_name_id=" +
            "(select id from project_management where name=#{projectName} and category='project')")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    List<JmxFileDescription> queryJmxFileByProjectName(String projectName);

    @Select("select * from jmx_file_description where project_name_id=#{projectNameId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    List<JmxFileDescription> queryJmxFileByProjectNameId(String projectNameId);

    @Select({"<script> select * from jmx_file_description where uuid in",
            "<foreach collection='uuids' item='uuid' open='(' separator=',' close=')'>",
            "#{uuid}",
            "</foreach>",
            "</script>"})
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name_id", property = "projectNameId"),
            @Result(column = "business_name_id", property = "businessNameId"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "content", property = "content")
    })
    List<JmxFileDescription> queryJmxFileByUuidList(Set uuids);

    @Update("update jmx_file_description set update_time=#{updateTime},content=#{content} where uuid=#{uuid}")
    int updateJmxFiles(JmxFileDescription jmxFileDescription);

}
