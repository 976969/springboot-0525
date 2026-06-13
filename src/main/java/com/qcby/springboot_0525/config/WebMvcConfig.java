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
        // 将上传文件的磁盘路径映射为URL访问路径
        // 前端通过 /result/xxx 访问实际文件 {upload-path}/result/xxx
        registry.addResourceHandler("/result/**")
                .addResourceLocations("file:" + uploadPath + "result/");
    }
}
