package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Student;
import com.qcby.springboot_0525.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StudentService {

    @Resource
    private StudentMapper studentMapper;

    public List<Student> list() {
        List<Student> list = studentMapper.selectList();
        list.forEach(s -> s.setPassword(null));
        return list;
    }

    public Student getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student != null) student.setPassword(null);
        return student;
    }

    public void add(Student student) {
        Student exist = studentMapper.selectByUsername(student.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        student.setStatus(1);
        studentMapper.insert(student);
    }

    public void update(Student student) {
        studentMapper.updateById(student);
    }

    public Student getRawById(Long id) {
        return studentMapper.selectById(id);
    }

    public void delete(Long id) {
        studentMapper.deleteById(id);
    }
}