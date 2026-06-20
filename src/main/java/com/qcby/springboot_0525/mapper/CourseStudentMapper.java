package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.CourseStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程学生关联数据访问层
 */
@Mapper
public interface CourseStudentMapper {

    /** 根据课程ID查询选课学生列表 */
    List<CourseStudent> selectByCourseId(@Param("courseId") Long courseId);

    /** 根据学生ID查询其选修的课程列表 */
    List<CourseStudent> selectByStudentId(@Param("studentId") Long studentId);

    /** 插入选课记录 */
    int insert(CourseStudent courseStudent);

    /** 根据ID删除选课记录 */
    int deleteById(@Param("id") Long id);

    /** 根据课程和学生ID删除选课记录 */
    int deleteByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
}
