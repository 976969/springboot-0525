package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Teacher;
import com.qcby.springboot_0525.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 教师业务逻辑层
 */
@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    /** 查询教师列表（隐藏密码） */
    public List<Teacher> list() {
        List<Teacher> list = teacherMapper.selectList();
        list.forEach(t -> t.setPassword(null));
        return list;
    }

    /** 根据ID查询教师（隐藏密码） */
    public Teacher getById(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher != null) teacher.setPassword(null);
        return teacher;
    }

    /** 添加教师（校验用户名唯一性） */
    public void add(Teacher teacher) {
        Teacher exist = teacherMapper.selectByUsername(teacher.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        teacher.setStatus(1);
        teacherMapper.insert(teacher);
    }

    /** 更新教师信息 */
    public void update(Teacher teacher) {
        teacherMapper.updateById(teacher);
    }

    /** 根据ID查询教师（包含密码，用于登录验证） */
    public Teacher getRawById(Long id) {
        return teacherMapper.selectById(id);
    }

    /** 删除教师 */
    public void delete(Long id) {
        teacherMapper.deleteById(id);
    }
}