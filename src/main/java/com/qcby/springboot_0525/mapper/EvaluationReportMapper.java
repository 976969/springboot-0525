package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationReportMapper {

    EvaluationReport selectById(@Param("id") Long id);

    List<EvaluationReport> selectList();

    List<EvaluationReport> selectByTaskId(@Param("taskId") Long taskId);

    List<EvaluationReport> selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    List<EvaluationReport> selectByTeacherId(@Param("teacherId") Long teacherId);

    int insert(EvaluationReport report);

    int updateById(EvaluationReport report);

    int deleteById(@Param("id") Long id);
}
