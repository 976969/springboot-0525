package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {

    Student selectById(@Param("id") Long id);

    Student selectByUsername(@Param("username") String username);

    List<Student> selectList();

    int insert(Student student);

    int updateById(Student student);

    int deleteById(@Param("id") Long id);
}