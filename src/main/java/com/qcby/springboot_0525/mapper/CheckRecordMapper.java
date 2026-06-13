package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.CheckRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CheckRecordMapper {

    CheckRecord selectById(@Param("id") Long id);

    List<CheckRecord> selectByResultId(@Param("resultId") Long resultId);

    int insert(CheckRecord record);

    int deleteByResultId(@Param("resultId") Long resultId);
}
