package com.example.springbootapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 공통 설정. Thymeleaf·정적 리소스는 application.properties 참고.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}
