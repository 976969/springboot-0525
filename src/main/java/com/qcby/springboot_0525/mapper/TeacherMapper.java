package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    Teacher selectById(@Param("id") Long id);

    Teacher selectByUsername(@Param("username") String username);

    List<Teacher> selectList();

    int insert(Teacher teacher);

    int updateById(Teacher teacher);

    int deleteById(@Param("id") Long id);
}