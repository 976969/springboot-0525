package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.Banner;
import com.qcby.springboot_0525.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @Resource
    private BannerMapper bannerMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 上传banner图片
     */
    @SaCheckRole("admin")
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".png";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String dirPath = uploadPath.endsWith("/") || uploadPath.endsWith("\\")
                ? uploadPath + "banner/" : uploadPath + "/banner/";
        File dir = new File(dirPath).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建目录失败: " + dir.getAbsolutePath());
        }
        try {
            File destFile = new File(dir, fileName);
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
        return Result.success("上传成功", "/api/banner-image/" + fileName);
    }

    /**
     * 获取已上架的轮播图列表（教师/学生端可见）
     */
    @GetMapping("/active")
    public Result<List<Banner>> getActiveBanners() {
        return Result.success(bannerMapper.selectActive());
    }

    /**
     * 获取全部轮播图列表（管理员端）
     */
    @SaCheckRole("admin")
    @GetMapping("/list")
    public Result<List<Banner>> getAllBanners() {
        return Result.success(bannerMapper.selectAll());
    }

    /**
     * 新增轮播图
     */
    @SaCheckRole("admin")
    @PostMapping
    public Result<Void> add(@RequestBody Banner banner) {
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        bannerMapper.insert(banner);
        return Result.success();
    }

    /**
     * 修改轮播图
     */
    @SaCheckRole("admin")
    @PutMapping
    public Result<Void> update(@RequestBody Banner banner) {
        if (banner.getId() == null) {
            throw new BusinessException("ID不能为空");
        }
        bannerMapper.updateById(banner);
        return Result.success();
    }

    /**
     * 删除轮播图
     */
    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }
}
