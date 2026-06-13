package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {

    Course selectById(@Param("id") Long id);

    List<Course> selectList();

    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);

    int insert(Course course);

    int updateById(Course course);

    int deleteById(@Param("id") Long id);
}
