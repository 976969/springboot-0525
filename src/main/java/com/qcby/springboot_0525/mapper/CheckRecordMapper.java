package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.CheckRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 核查记录数据访问层
 */
@Mapper
public interface CheckRecordMapper {

    /** 根据ID查询核查记录 */
    CheckRecord selectById(@Param("id") Long id);

    /** 根据成果ID查询核查记录列表 */
    List<CheckRecord> selectByResultId(@Param("resultId") Long resultId);

    /** 插入核查记录 */
    int insert(CheckRecord record);

    /** 根据成果ID删除核查记录 */
    int deleteByResultId(@Param("resultId") Long resultId);
}
