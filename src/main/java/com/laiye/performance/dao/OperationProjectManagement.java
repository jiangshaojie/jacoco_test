package com.laiye.performance.dao;

import com.laiye.performance.enity.ProjectManagement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationProjectManagement {
    @Insert("insert into project_management (name ,business_name,category) values (#{name},#{businessName}," +
            "#{category})")
    public int insertProjectManagement(ProjectManagement projectManagement);

    @Select("select * from project_management where name=#{name} and " +
            "business_name=#{businessName} and category=#{category}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    public ProjectManagement queryByNameAndBusinessNameAndCategory(String name, String businessName, String category);

    @Select("select * from project_management where name=#{projectName} and category='project'")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    public ProjectManagement queryProjectByName(String projectName);

    @Select("select * from project_management where id=#{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    public ProjectManagement queryProjectById(String id);

    @Select("select * from project_management where category='project'")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    List<ProjectManagement> queryProject();

    @Select("select * from project_management where category='case'")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    List<ProjectManagement> queryCaseTab();

    @Select("select * from project_management where category='plan'")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "business_name", property = "businessName"),
            @Result(column = "category", property = "category")
    })
    List<ProjectManagement> queryPlanTab();


    @Select("select * from  project_management where category='project'")
    @Results({
            @Result(column = "name", property = "name")
    })
    List<ProjectManagement> queryProjectName();
}
