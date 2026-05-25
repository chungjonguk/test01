package com.example.springbootapp.config.web;

import java.lang.reflect.Method;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 등록된 {@link RequestMappingInfo} 경로에 {@code .do} 접미사를 적용합니다.
 */
public class DoSuffixRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
		if (info == null || !DoPathHelper.appliesToController(handlerType)) {
			return info;
		}
		return DoPathHelper.addDoSuffix(info);
	}
}
