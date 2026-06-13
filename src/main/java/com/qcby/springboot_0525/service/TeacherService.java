package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Teacher;
import com.qcby.springboot_0525.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    public List<Teacher> list() {
        List<Teacher> list = teacherMapper.selectList();
        list.forEach(t -> t.setPassword(null));
        return list;
    }

    public Teacher getById(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher != null) teacher.setPassword(null);
        return teacher;
    }

    public void add(Teacher teacher) {
        Teacher exist = teacherMapper.selectByUsername(teacher.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        teacher.setStatus(1);
        teacherMapper.insert(teacher);
    }

    public void update(Teacher teacher) {
        teacherMapper.updateById(teacher);
    }

    public Teacher getRawById(Long id) {
        return teacherMapper.selectById(id);
    }

    public void delete(Long id) {
        teacherMapper.deleteById(id);
    }
}