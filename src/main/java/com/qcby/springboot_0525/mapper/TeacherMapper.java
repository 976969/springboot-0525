package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师数据访问层
 */
@Mapper
public interface TeacherMapper {

    /** 根据ID查询教师 */
    Teacher selectById(@Param("id") Long id);

    /** 根据用户名查询教师 */
    Teacher selectByUsername(@Param("username") String username);

    /** 查询所有教师列表 */
    List<Teacher> selectList();

    /** 插入教师记录 */
    int insert(Teacher teacher);

    /** 根据ID更新教师信息 */
    int updateById(Teacher teacher);

    /** 根据手机号查询教师 */
    Teacher selectByPhone(@Param("phone") String phone);

    /** 根据邮箱查询教师 */
    Teacher selectByEmail(@Param("email") String email);

    /** 根据ID删除教师 */
    int deleteById(@Param("id") Long id);
}