package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.entity.EvaluationIndicator;
import com.qcby.springboot_0525.mapper.EvaluationIndicatorMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EvaluationIndicatorService {

    @Resource
    private EvaluationIndicatorMapper indicatorMapper;

    public EvaluationIndicator getById(Long id) {
        return indicatorMapper.selectById(id);
    }

    public List<EvaluationIndicator> list() {
        return indicatorMapper.selectList();
    }

    public void add(EvaluationIndicator indicator) {
        indicator.setIsSystem(0);
        indicatorMapper.insert(indicator);
    }

    public void update(EvaluationIndicator indicator) {
        indicatorMapper.updateById(indicator);
    }

    public void delete(Long id) {
        EvaluationIndicator indicator = indicatorMapper.selectById(id);
        if (indicator != null && indicator.getIsSystem() == 1) {
            throw new com.qcby.springboot_0525.common.BusinessException("系统内置指标不可删除");
        }
        indicatorMapper.deleteById(id);
    }
}
