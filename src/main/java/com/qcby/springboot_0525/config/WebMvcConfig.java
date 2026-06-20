package com.qcby.springboot_0525.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置 - 本地上传文件访问映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = uploadPath.endsWith("/") || uploadPath.endsWith("\\")
                ? uploadPath : uploadPath + "/";
        // 将上传文件的磁盘路径映射为URL访问路径
        registry.addResourceHandler("/result/**")
                .addResourceLocations("file:" + basePath + "result/");
        // banner图片访问映射（用/banner-image/避免与BannerController的/{id}冲突）
        registry.addResourceHandler("/banner-image/**")
                .addResourceLocations("file:" + basePath + "banner/");
    }
}
