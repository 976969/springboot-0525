package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价报告数据访问层
 */
@Mapper
public interface EvaluationReportMapper {

    /** 根据ID查询报告 */
    EvaluationReport selectById(@Param("id") Long id);

    /** 查询所有报告列表 */
    List<EvaluationReport> selectList();

    /** 根据任务ID查询报告列表 */
    List<EvaluationReport> selectByTaskId(@Param("taskId") Long taskId);

    /** 根据任务和学生ID查询报告 */
    List<EvaluationReport> selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    /** 根据教师ID查询其学生的报告列表 */
    List<EvaluationReport> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 插入报告记录 */
    int insert(EvaluationReport report);

    /** 根据ID更新报告信息 */
    int updateById(EvaluationReport report);

    /** 根据ID删除报告 */
    int deleteById(@Param("id") Long id);
}
