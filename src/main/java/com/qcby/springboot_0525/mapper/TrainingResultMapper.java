package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.TrainingResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实训成果数据访问层
 */
@Mapper
public interface TrainingResultMapper {

    /** 根据ID查询成果 */
    TrainingResult selectById(@Param("id") Long id);

    /** 查询所有成果列表 */
    List<TrainingResult> selectList();

    /** 根据任务ID查询成果列表 */
    List<TrainingResult> selectByTaskId(@Param("taskId") Long taskId);

    /** 根据学生ID查询成果列表 */
    List<TrainingResult> selectByStudentId(@Param("studentId") Long studentId);

    /** 根据教师ID查询其学生的成果列表 */
    List<TrainingResult> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 根据任务和学生ID查询成果（防重复提交校验） */
    TrainingResult selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    /** 插入成果记录 */
    int insert(TrainingResult result);

    /** 根据ID更新成果信息 */
    int updateById(TrainingResult result);

    /** 根据ID删除成果 */
    int deleteById(@Param("id") Long id);
}
