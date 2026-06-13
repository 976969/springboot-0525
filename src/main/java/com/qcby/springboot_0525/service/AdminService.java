package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Admin;
import com.qcby.springboot_0525.mapper.AdminMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    public List<Admin> list() {
        List<Admin> list = adminMapper.selectList();
        list.forEach(a -> a.setPassword(null));
        return list;
    }

    public Admin getById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    public void add(Admin admin) {
        Admin exist = adminMapper.selectByUsername(admin.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    public Admin getRawById(Long id) {
        return adminMapper.selectById(id);
    }

    public void delete(Long id) {
        adminMapper.deleteById(id);
    }
}