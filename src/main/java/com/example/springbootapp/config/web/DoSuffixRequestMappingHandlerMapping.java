package com.example.springbootapp.config.web;

import java.lang.reflect.Method;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 등록된 {@link RequestMappingInfo} 경로를 {@code /e/{암호문}.do} 공개 URL로 등록합니다.
 */
public class DoSuffixRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private PublicPathCryptoService publicPathCryptoService;

	public void setPublicPathCryptoService(PublicPathCryptoService publicPathCryptoService) {
		this.publicPathCryptoService = publicPathCryptoService;
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
		if (info == null || !DoPathHelper.appliesToController(handlerType)) {
			return info;
		}
		if (publicPathCryptoService != null) {
			return publicPathCryptoService.addEncryptedMapping(info);
		}
		return DoPathHelper.addDoSuffix(info);
	}
}
