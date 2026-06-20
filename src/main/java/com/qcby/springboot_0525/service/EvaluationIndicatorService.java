package com.qcby.springboot_0525.service;

import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.entity.EvaluationIndicator;
import com.qcby.springboot_0525.mapper.EvaluationIndicatorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评价指标业务逻辑层
 */
@Service
public class EvaluationIndicatorService {

    @Resource
    private EvaluationIndicatorMapper indicatorMapper;

    /** 根据ID查询指标 */
    public EvaluationIndicator getById(Long id) {
        return indicatorMapper.selectById(id);
    }

    /** 查询所有指标列表 */
    public List<EvaluationIndicator> list() {
        return indicatorMapper.selectList();
    }

    /** 根据教师ID查询指标列表 */
    public List<EvaluationIndicator> listByTeacherId(Long teacherId) {
        return indicatorMapper.selectByTeacherId(teacherId);
    }

    /** 查询系统默认指标模板 */
    public List<EvaluationIndicator> getSystemTemplate() {
        return indicatorMapper.selectSystemTemplate();
    }

    /**
     * 初始化教师的指标（首次登录时调用）
     * 从系统模板复制一份到教师的指标库
     */
    @Transactional
    public void initTeacherIndicators(Long teacherId) {
        // 检查是否已经初始化
        List<EvaluationIndicator> existing = indicatorMapper.selectByTeacherId(teacherId);
        if (!existing.isEmpty()) {
            return; // 已经初始化，跳过
        }

        // 从系统模板复制
        List<EvaluationIndicator> template = indicatorMapper.selectSystemTemplate();
        for (EvaluationIndicator indicator : template) {
            EvaluationIndicator newIndicator = new EvaluationIndicator();
            newIndicator.setName(indicator.getName());
            newIndicator.setTeacherId(teacherId);
            newIndicator.setDescription(indicator.getDescription());
            newIndicator.setCategory(indicator.getCategory());
            newIndicator.setDefaultWeight(indicator.getDefaultWeight());
            newIndicator.setOriginalWeight(indicator.getOriginalWeight());
            newIndicator.setIsSystem(indicator.getIsSystem());
            indicatorMapper.insert(newIndicator);
        }
    }

    /** 添加指标（记录原始权重用于重置） */
    public void add(EvaluationIndicator indicator) {
        if (indicator.getIsSystem() == null) {
            indicator.setIsSystem(0);
        }
        // 原始权重记录当前权重值（用于重置）
        indicator.setOriginalWeight(indicator.getDefaultWeight());
        indicatorMapper.insert(indicator);
    }

    /** 更新指标信息 */
    public void update(EvaluationIndicator indicator) {
        indicatorMapper.updateById(indicator);
    }

    /** 删除指标（系统指标不可删除） */
    public void delete(Long id) {
        EvaluationIndicator indicator = indicatorMapper.selectById(id);
        if (indicator != null && indicator.getIsSystem() == 1) {
            throw new BusinessException("系统内置指标不可删除");
        }
        indicatorMapper.deleteById(id);
    }

    /** 重置所有指标权重为原始值 */
    public void resetWeights() {
        indicatorMapper.resetWeights();
    }

    /** 重置指定教师的指标权重 */
    public void resetTeacherWeights(Long teacherId) {
        List<EvaluationIndicator> indicators = indicatorMapper.selectByTeacherId(teacherId);
        for (EvaluationIndicator indicator : indicators) {
            if (indicator.getIsSystem() == 1) {
                // 系统指标：恢复原始权重
                indicator.setDefaultWeight(indicator.getOriginalWeight());
            } else {
                // 自定义指标：权重设为0
                indicator.setDefaultWeight(java.math.BigDecimal.ZERO);
            }
            indicatorMapper.updateById(indicator);
        }
    }
}
