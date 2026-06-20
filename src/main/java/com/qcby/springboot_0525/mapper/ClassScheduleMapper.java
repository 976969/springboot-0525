package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.ClassSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程安排数据访问层
 */
@Mapper
public interface ClassScheduleMapper {

    /** 根据教师ID查询课程安排列表 */
    List<ClassSchedule> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 查询所有课程安排 */
    List<ClassSchedule> selectAll();

    /** 根据ID查询课程安排 */
    ClassSchedule selectById(@Param("id") Long id);

    /** 插入课程安排记录 */
    int insert(ClassSchedule schedule);

    /** 根据ID更新课程安排 */
    int updateById(ClassSchedule schedule);

    /** 根据ID删除课程安排 */
    int deleteById(@Param("id") Long id);
}
