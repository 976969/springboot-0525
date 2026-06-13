package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.TrainingTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingTaskMapper {

    TrainingTask selectById(@Param("id") Long id);

    List<TrainingTask> selectList();

    List<TrainingTask> selectByCourseId(@Param("courseId") Long courseId);

    List<TrainingTask> selectByTeacherId(@Param("teacherId") Long teacherId);

    int insert(TrainingTask task);

    int updateById(TrainingTask task);

    int deleteById(@Param("id") Long id);
}
