package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生数据访问层
 */
@Mapper
public interface StudentMapper {

    /** 根据ID查询学生 */
    Student selectById(@Param("id") Long id);

    /** 根据用户名查询学生 */
    Student selectByUsername(@Param("username") String username);

    /** 查询所有学生列表 */
    List<Student> selectList();

    /** 插入学生记录 */
    int insert(Student student);

    /** 根据ID更新学生信息 */
    int updateById(Student student);

    /** 根据手机号查询学生 */
    Student selectByPhone(@Param("phone") String phone);

    /** 根据邮箱查询学生 */
    Student selectByEmail(@Param("email") String email);

    /** 根据ID删除学生 */
    int deleteById(@Param("id") Long id);
}