package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.TrainingResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingResultMapper {

    TrainingResult selectById(@Param("id") Long id);

    List<TrainingResult> selectList();

    List<TrainingResult> selectByTaskId(@Param("taskId") Long taskId);

    List<TrainingResult> selectByStudentId(@Param("studentId") Long studentId);

    List<TrainingResult> selectByTeacherId(@Param("teacherId") Long teacherId);

    TrainingResult selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    int insert(TrainingResult result);

    int updateById(TrainingResult result);

    int deleteById(@Param("id") Long id);
}
