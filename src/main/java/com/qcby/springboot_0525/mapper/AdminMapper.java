package com.qcby.springboot_0525.mapper;

import com.qcby.springboot_0525.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员数据访问层
 */
@Mapper
public interface AdminMapper {

    /** 根据ID查询管理员 */
    Admin selectById(@Param("id") Long id);

    /** 根据用户名查询管理员 */
    Admin selectByUsername(@Param("username") String username);

    /** 查询所有管理员列表 */
    List<Admin> selectList();

    /** 插入管理员记录 */
    int insert(Admin admin);

    /** 根据ID更新管理员信息 */
    int updateById(Admin admin);

    /** 根据ID删除管理员 */
    int deleteById(@Param("id") Long id);
}