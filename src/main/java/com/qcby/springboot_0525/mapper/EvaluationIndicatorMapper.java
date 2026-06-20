package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价指标数据访问层
 */
@Mapper
public interface EvaluationIndicatorMapper {

    /** 根据ID查询指标 */
    EvaluationIndicator selectById(@Param("id") Long id);

    /** 查询所有指标列表 */
    List<EvaluationIndicator> selectList();

    /** 根据教师ID查询指标列表 */
    List<EvaluationIndicator> selectByTeacherId(@Param("teacherId") Long teacherId);

    /** 查询系统默认指标模板 */
    List<EvaluationIndicator> selectSystemTemplate();

    /** 插入指标记录 */
    int insert(EvaluationIndicator indicator);

    /** 根据ID更新指标信息 */
    int updateById(EvaluationIndicator indicator);

    /** 根据ID删除指标 */
    int deleteById(@Param("id") Long id);

    /** 重置所有指标权重为原始值 */
    int resetWeights();
}
