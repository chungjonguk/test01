package com.example.springbootapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.springbootapp.web.ScreenAccessInterceptor;

@Profile("!test")
@Configuration
public class WebMvcScreenConfig implements WebMvcConfigurer {

	private final ScreenAccessInterceptor screenAccessInterceptor;

	public WebMvcScreenConfig(ScreenAccessInterceptor screenAccessInterceptor) {
		this.screenAccessInterceptor = screenAccessInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(screenAccessInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/api/**",
						"/auth/**",
						"/assets/**",
						"/vendors/**",
						"/error/**");
	}
}
