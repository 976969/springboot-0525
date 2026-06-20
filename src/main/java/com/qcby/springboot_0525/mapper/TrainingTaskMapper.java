package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.TrainingTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实训任务数据访问层
 */
@Mapper
public interface TrainingTaskMapper {

    /** 根据ID查询任务 */
    TrainingTask selectById(@Param("id") Long id);

    /** 查询所有任务列表 */
    List<TrainingTask> selectList();

    /** 查询未过期的任务列表 */
    List<TrainingTask> selectActiveList();

    /** 根据课程ID查询任务列表 */
    List<TrainingTask> selectByCourseId(@Param("courseId") Long courseId);

    /** 根据教师ID查询任务列表 */
    List<TrainingTask> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 插入任务记录 */
    int insert(TrainingTask task);

    /** 根据ID更新任务信息 */
    int updateById(TrainingTask task);

    /** 根据ID删除任务 */
    int deleteById(@Param("id") Long id);
}
