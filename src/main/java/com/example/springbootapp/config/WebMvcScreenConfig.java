package com.example.springbootapp.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.springbootapp.web.ScreenAccessInterceptor;
import com.example.springbootapp.web.UserAccessLogInterceptor;
@Profile("!test")
@Configuration
public class WebMvcScreenConfig implements WebMvcConfigurer {
	private final ScreenAccessInterceptor screenAccessInterceptor;
	private final UserAccessLogInterceptor userAccessLogInterceptor;
	public WebMvcScreenConfig(
			ScreenAccessInterceptor screenAccessInterceptor,
			UserAccessLogInterceptor userAccessLogInterceptor) {
		this.screenAccessInterceptor = screenAccessInterceptor;
		this.userAccessLogInterceptor = userAccessLogInterceptor;
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
		registry.addInterceptor(userAccessLogInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/api/**",
						"/auth/**",
						"/assets/**",
						"/vendors/**",
						"/error/**");
	}
}
