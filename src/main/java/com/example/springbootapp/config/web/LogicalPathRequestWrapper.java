package com.example.springbootapp.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 암호화 URL을 논리 경로로 풀어 DispatcherServlet에 전달합니다.
 */
final class LogicalPathRequestWrapper extends HttpServletRequestWrapper {

	private final String logicalPath;
	private final String requestUri;

	LogicalPathRequestWrapper(HttpServletRequest request, String contextPath, String logicalPath) {
		super(request);
		this.logicalPath = logicalPath.startsWith("/") ? logicalPath : "/" + logicalPath;
		String ctx = contextPath != null ? contextPath : "";
		this.requestUri = ctx + this.logicalPath;
	}

	@Override
	public String getRequestURI() {
		return requestUri;
	}

	@Override
	public String getServletPath() {
		return logicalPath;
	}
}
