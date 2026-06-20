package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.mapper.TrainingTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 实训任务业务逻辑层
 */
@Service
public class TrainingTaskService {

    @Resource
    private TrainingTaskMapper trainingTaskMapper;

    /** 根据ID查询任务 */
    public TrainingTask getById(Long id) {
        return trainingTaskMapper.selectById(id);
    }

    /** 查询所有任务列表 */
    public List<TrainingTask> list() {
        return trainingTaskMapper.selectList();
    }

    /** 查询未过期的任务列表（学生端用） */
    public List<TrainingTask> listActive() {
        return trainingTaskMapper.selectActiveList();
    }

    /** 根据教师ID查询任务列表 */
    public List<TrainingTask> listByTeacherId(Long teacherId) {
        return trainingTaskMapper.selectByTeacherId(teacherId);
    }

    /** 根据课程ID查询任务列表 */
    public List<TrainingTask> listByCourseId(Long courseId) {
        return trainingTaskMapper.selectByCourseId(courseId);
    }

    /** 添加任务 */
    public void add(TrainingTask task) {
        task.setStatus(1);
        trainingTaskMapper.insert(task);
    }

    /** 更新任务信息 */
    public void update(TrainingTask task) {
        trainingTaskMapper.updateById(task);
    }

    /** 删除任务 */
    public void delete(Long id) {
        trainingTaskMapper.deleteById(id);
    }
}
