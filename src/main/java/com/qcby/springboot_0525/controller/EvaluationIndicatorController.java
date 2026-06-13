package com.qcby.springboot_0525.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qcby.springboot_0525.common.PageResult;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.EvaluationIndicator;
import com.qcby.springboot_0525.service.EvaluationIndicatorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/indicator")
public class EvaluationIndicatorController {

    @Resource
    private EvaluationIndicatorService indicatorService;

    @GetMapping("/page")
    public Result<PageResult<EvaluationIndicator>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<EvaluationIndicator> list = indicatorService.list();
        PageInfo<EvaluationIndicator> pageInfo = new PageInfo<>(list);
        return Result.success(PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<List<EvaluationIndicator>> listAll() {
        return Result.success(indicatorService.list());
    }

    @GetMapping("/{id}")
    public Result<EvaluationIndicator> getById(@PathVariable Long id) {
        return Result.success(indicatorService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody EvaluationIndicator indicator) {
        indicatorService.add(indicator);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody EvaluationIndicator indicator) {
        indicatorService.update(indicator);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        indicatorService.delete(id);
        return Result.success();
    }
}
