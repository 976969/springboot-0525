package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.EvaluationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评价记录数据访问层
 */
@Mapper
public interface EvaluationRecordMapper {

    /** 根据ID查询评价记录 */
    EvaluationRecord selectById(@Param("id") Long id);

    /** 查询所有评价记录列表 */
    List<EvaluationRecord> selectList();

    /** 根据成果ID查询评价记录列表 */
    List<EvaluationRecord> selectByResultId(@Param("resultId") Long resultId);

    /** 插入评价记录 */
    int insert(EvaluationRecord record);

    /** 根据ID更新评价记录 */
    int updateById(EvaluationRecord record);

    /** 根据成果ID批量更新评分比重 */
    int updateScoreRatioByResultId(@Param("resultId") Long resultId, @Param("scoreRatio") BigDecimal scoreRatio);

    /** 根据成果ID删除评价记录 */
    int deleteByResultId(@Param("resultId") Long resultId);
}
