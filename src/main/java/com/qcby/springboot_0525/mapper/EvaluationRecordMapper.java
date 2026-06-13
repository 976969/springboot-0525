package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationRecordMapper {

    EvaluationRecord selectById(@Param("id") Long id);

    List<EvaluationRecord> selectList();

    List<EvaluationRecord> selectByResultId(@Param("resultId") Long resultId);

    int insert(EvaluationRecord record);

    int updateById(EvaluationRecord record);

    int deleteByResultId(@Param("resultId") Long resultId);
}
