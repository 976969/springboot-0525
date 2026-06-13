package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Course;
import com.qcby.springboot_0525.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CourseService {

    @Resource
    private CourseMapper courseMapper;

    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    public List<Course> list() {
        return courseMapper.selectList();
    }

    public List<Course> listByTeacherId(Long teacherId) {
        return courseMapper.selectByTeacherId(teacherId);
    }

    public void add(Course course) {
        course.setStatus(1);
        courseMapper.insert(course);
    }

    public void update(Course course) {
        courseMapper.updateById(course);
    }

    public void delete(Long id) {
        courseMapper.deleteById(id);
    }
}
