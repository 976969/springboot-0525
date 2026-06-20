package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Admin;
import com.qcby.springboot_0525.mapper.AdminMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理员业务逻辑层
 */
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    /** 查询管理员列表（隐藏密码） */
    public List<Admin> list() {
        List<Admin> list = adminMapper.selectList();
        list.forEach(a -> a.setPassword(null));
        return list;
    }

    /** 根据ID查询管理员（隐藏密码） */
    public Admin getById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    /** 添加管理员（校验用户名唯一性） */
    public void add(Admin admin) {
        Admin exist = adminMapper.selectByUsername(admin.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    /** 更新管理员信息 */
    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    /** 根据ID查询管理员（包含密码，用于登录验证） */
    public Admin getRawById(Long id) {
        return adminMapper.selectById(id);
    }

    /** 删除管理员 */
    public void delete(Long id) {
        adminMapper.deleteById(id);
    }
}