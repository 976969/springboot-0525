package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.AiPractice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI练习题Mapper
 */
@Mapper
public interface AiPracticeMapper {

    /** 新增练习记录 */
    int insert(AiPractice practice);

    /** 根据ID查询 */
    AiPractice selectById(@Param("id") Long id);

    /** 查询某学生的练习记录 */
    List<AiPractice> selectByStudentId(@Param("studentId") Long studentId);

    /** 查询某学生某课程的练习记录 */
    List<AiPractice> selectByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /** 更新答案和得分 */
    int updateScore(@Param("id") Long id, @Param("answers") String answers, @Param("score") Double score);
}
