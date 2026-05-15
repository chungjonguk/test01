package com.example.springbootapp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.example.springbootapp.mapper")
public class MybatisConfig {
}
