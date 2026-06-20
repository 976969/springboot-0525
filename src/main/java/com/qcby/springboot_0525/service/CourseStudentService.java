package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.CourseStudent;
import com.qcby.springboot_0525.mapper.CourseStudentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程学生关联业务逻辑层
 */
@Service
public class CourseStudentService {

    @Resource
    private CourseStudentMapper courseStudentMapper;

    /** 查询课程下的学生列表 */
    public List<CourseStudent> getStudentsByCourseId(Long courseId) {
        return courseStudentMapper.selectByCourseId(courseId);
    }

    /** 查询学生选修的课程列表 */
    public List<CourseStudent> getCoursesByStudentId(Long studentId) {
        return courseStudentMapper.selectByStudentId(studentId);
    }

    /** 添加学生到课程（校验重复选课） */
    public void addStudentToCourse(Long courseId, Long studentId) {
        // 检查是否已存在
        List<CourseStudent> existing = courseStudentMapper.selectByCourseId(courseId);
        for (CourseStudent cs : existing) {
            if (cs.getStudentId().equals(studentId)) {
                throw new BusinessException("该学生已在此课程中");
            }
        }
        CourseStudent cs = new CourseStudent();
        cs.setCourseId(courseId);
        cs.setStudentId(studentId);
        courseStudentMapper.insert(cs);
    }

    /** 从课程移除学生 */
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        courseStudentMapper.deleteByCourseAndStudent(courseId, studentId);
    }

    /** 根据ID删除选课记录 */
    public void deleteById(Long id) {
        courseStudentMapper.deleteById(id);
    }
}
