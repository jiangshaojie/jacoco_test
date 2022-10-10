package com.laiye.performance.dao;

import com.laiye.performance.enity.Cases;
import com.laiye.performance.enity.Tag;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OperationTag {

    @Insert("insert into Tag (name,project_management_id)" +
            "values (#{name}," +
            "(select id from project_management a where a.name=#{projectName} and a.business_name=#{tabName} and a.category=#{category}) )")
    int insertTag(String name, String projectName, String tabName, String category);

    @Update("update cases set name=#{name}, config=#{config},update_time=#{updateTime} where uuid=#{uuid}")
    int updateCases(Cases cases);

    @Select("select * from cases where uuid==#{uuid}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "project_name", property = "projectName"),
            @Result(column = "business_name", property = "businessName"),
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
            @Result(column = "project_name", property = "projectName")
    })
    List<Cases> queryCasesByUuids(Set uuids);

    @Select("select a.id,a.name name, b.name jmx_uuid, a.config, a.uuid, a.update_time, a.project_name, a.business_name,a.create_time" +
            "  from cases as a," +
            "  jmx_file_description as b" +
            "  where a.project_name = #{projectName}" +
            "  and a.jmx_uuid = b.uuid")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "project_name", property = "projectName"),
            @Result(column = "business_name", property = "businessName"),
    })
    List<Cases> queryCasesByProjectName(String projectName);

    @Select("select a.id,a.name name, b.name jmx_uuid, a.config, a.uuid, a.update_time, a.project_name, a.business_name,a.create_time" +
            "  from cases as a," +
            "  jmx_file_description as b" +
            "  where a.project_name = #{projectName} and a.business_name = #{businessName}" +
            "  and a.jmx_uuid = b.uuid")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "jmx_uuid", property = "jmxUuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "config", property = "config"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "project_name", property = "projectName"),
            @Result(column = "business_name", property = "businessName"),
    })
    List<Cases> queryCasesByProjectNameAndBusinessName(String projectName, String businessName);

    @Select("select * from tag a where a.project_management_id=(select id from project_management b where " +
            "b.name=#{projectName} and b.business_name=#{tabName} and b.category=#{category})")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "project_management_id", property = "projectManagementId")
    })
    List<Tag> queryTag(String projectName, String tabName, String category);

    @Delete("delete from Tag where id=#{id}")
    int deleteTag(String id);
}
