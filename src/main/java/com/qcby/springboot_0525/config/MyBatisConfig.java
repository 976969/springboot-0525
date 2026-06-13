package com.qcby.springboot_0525.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置
 */
@Configuration
@MapperScan("com.qcby.springboot_0525.mapper")
public class MyBatisConfig {
}
