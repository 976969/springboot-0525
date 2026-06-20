package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.entity.ClassSchedule;
import com.qcby.springboot_0525.mapper.ClassScheduleMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程安排业务逻辑层
 */
@Service
public class ClassScheduleService {

    @Resource
    private ClassScheduleMapper scheduleMapper;

    /** 根据教师ID查询课程安排 */
    public List<ClassSchedule> listByTeacherId(Long teacherId) {
        return scheduleMapper.selectByTeacherId(teacherId);
    }

    /** 查询所有课程安排 */
    public List<ClassSchedule> listAll() {
        return scheduleMapper.selectAll();
    }

    /** 根据ID查询课程安排 */
    public ClassSchedule getById(Long id) {
        return scheduleMapper.selectById(id);
    }

    /** 添加课程安排 */
    public void add(ClassSchedule schedule) {
        scheduleMapper.insert(schedule);
    }

    /** 更新课程安排 */
    public void update(ClassSchedule schedule) {
        scheduleMapper.updateById(schedule);
    }

    /** 删除课程安排 */
    public void delete(Long id) {
        scheduleMapper.deleteById(id);
    }
}
