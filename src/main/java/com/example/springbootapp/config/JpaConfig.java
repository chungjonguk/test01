package com.example.springbootapp.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA는 shop_orders 데모 전용. 업무 도메인(사용자 등)은 MyBatis({@link com.example.springbootapp.mapper}) 사용.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.example.springbootapp.repository")
@EntityScan(basePackages = "com.example.springbootapp.entity")
public class JpaConfig {
}
