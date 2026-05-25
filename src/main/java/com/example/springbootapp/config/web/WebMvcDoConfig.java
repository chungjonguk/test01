package com.example.springbootapp.config.web;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * MVC·REST 컨트롤러 매핑 URL을 {@code *.do} 형식으로 등록합니다.
 */
@Configuration
@Profile("!test")
public class WebMvcDoConfig implements WebMvcRegistrations {

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new DoSuffixRequestMappingHandlerMapping();
	}
}
