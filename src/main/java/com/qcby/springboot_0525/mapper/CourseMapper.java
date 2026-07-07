package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程数据访问层
 */
@Mapper
public interface CourseMapper {

    /** 根据ID查询课程 */
    Course selectById(@Param("id") Long id);

    /** 查询所有课程列表 */
    List<Course> selectList();

    /** 根据教师ID查询其负责的课程列表 */
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 插入课程记录 */
    int insert(Course course);

    /** 根据ID更新课程信息 */
    int updateById(Course course);

    /** 根据ID删除课程 */
    int deleteById(@Param("id") Long id);

    /** 根据条件筛选课程列表（管理员用） */
    List<Course> selectFiltered(@Param("keyword") String keyword, @Param("teacherId") Long teacherId);
}
