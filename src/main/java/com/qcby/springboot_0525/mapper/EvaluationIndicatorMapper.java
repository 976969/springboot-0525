package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationIndicatorMapper {

    EvaluationIndicator selectById(@Param("id") Long id);

    List<EvaluationIndicator> selectList();

    int insert(EvaluationIndicator indicator);

    int updateById(EvaluationIndicator indicator);

    int deleteById(@Param("id") Long id);
}
