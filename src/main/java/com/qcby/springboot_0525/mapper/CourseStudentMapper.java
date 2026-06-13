package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.CourseStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseStudentMapper {

    List<CourseStudent> selectByCourseId(@Param("courseId") Long courseId);

    List<CourseStudent> selectByStudentId(@Param("studentId") Long studentId);

    int insert(CourseStudent courseStudent);

    int deleteById(@Param("id") Long id);

    int deleteByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
