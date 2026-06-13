package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.entity.TrainingTask;
import com.qcby.springboot_0525.mapper.TrainingTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainingTaskService {

    @Resource
    private TrainingTaskMapper trainingTaskMapper;

    public TrainingTask getById(Long id) {
        return trainingTaskMapper.selectById(id);
    }

    public List<TrainingTask> list() {
        return trainingTaskMapper.selectList();
    }

    public List<TrainingTask> listByTeacherId(Long teacherId) {
        return trainingTaskMapper.selectByTeacherId(teacherId);
    }

    public List<TrainingTask> listByCourseId(Long courseId) {
        return trainingTaskMapper.selectByCourseId(courseId);
    }

    public void add(TrainingTask task) {
        task.setStatus(1);
        trainingTaskMapper.insert(task);
    }

    public void update(TrainingTask task) {
        trainingTaskMapper.updateById(task);
    }

    public void delete(Long id) {
        trainingTaskMapper.deleteById(id);
    }
}
