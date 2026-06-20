package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Student;
import com.qcby.springboot_0525.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学生业务逻辑层
 */
@Service
public class StudentService {

    @Resource
    private StudentMapper studentMapper;

    /** 查询学生列表（隐藏密码） */
    public List<Student> list() {
        List<Student> list = studentMapper.selectList();
        list.forEach(s -> s.setPassword(null));
        return list;
    }

    /** 根据ID查询学生（隐藏密码） */
    public Student getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student != null) student.setPassword(null);
        return student;
    }

    /** 添加学生（校验用户名唯一性） */
    public void add(Student student) {
        Student exist = studentMapper.selectByUsername(student.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        student.setStatus(1);
        studentMapper.insert(student);
    }

    /** 更新学生信息 */
    public void update(Student student) {
        studentMapper.updateById(student);
    }

    /** 根据ID查询学生（包含密码，用于登录验证） */
    public Student getRawById(Long id) {
        return studentMapper.selectById(id);
    }

    /** 删除学生 */
    public void delete(Long id) {
        studentMapper.deleteById(id);
    }
}