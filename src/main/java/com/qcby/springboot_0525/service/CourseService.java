package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.Course;
import com.qcby.springboot_0525.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程业务逻辑层
 */
@Service
public class CourseService {

    @Resource
    private CourseMapper courseMapper;

    /** 根据ID查询课程 */
    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    /** 查询所有课程列表 */
    public List<Course> list() {
        return courseMapper.selectList();
    }

    /** 根据教师ID查询其负责的课程列表 */
    public List<Course> listByTeacherId(Long teacherId) {
        return courseMapper.selectByTeacherId(teacherId);
    }

    /** 添加课程 */
    public void add(Course course) {
        course.setStatus(1);
        courseMapper.insert(course);
    }

    /** 更新课程信息 */
    public void update(Course course) {
        courseMapper.updateById(course);
    }

    /** 删除课程 */
    public void delete(Long id) {
        courseMapper.deleteById(id);
    }
}
