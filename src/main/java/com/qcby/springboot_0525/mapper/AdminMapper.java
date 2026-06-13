package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {

    Admin selectById(@Param("id") Long id);

    Admin selectByUsername(@Param("username") String username);

    List<Admin> selectList();

    int insert(Admin admin);

    int updateById(Admin admin);

    int deleteById(@Param("id") Long id);
}