package com.example.springbootapp.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.service.UserAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 사용자의 화면(GET) 접속을 user_access_log(PAGE)에 기록합니다.
 */
@Profile("!test")
@Component
public class UserAccessLogInterceptor implements HandlerInterceptor {

	private final SessionAuthService sessionAuthService;
	private final UserAccessLogService userAccessLogService;
	private final boolean pageLogEnabled;

	public UserAccessLogInterceptor(
			SessionAuthService sessionAuthService,
			UserAccessLogService userAccessLogService,
			@Value("${app.access-log.page-enabled:true}") boolean pageLogEnabled) {
		this.sessionAuthService = sessionAuthService;
		this.userAccessLogService = userAccessLogService;
		this.pageLogEnabled = pageLogEnabled;
	}

	@Override
	public void afterCompletion(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception ex) {
		if (!pageLogEnabled || ex != null || response.getStatus() >= 400) {
			return;
		}
		if (!"GET".equalsIgnoreCase(request.getMethod()) || shouldSkip(request, handler)) {
			return;
		}
		LoginSession loginSession = sessionAuthService.getLoginSession(request.getSession(false));
		if (loginSession == null) {
			return;
		}
		userAccessLogService.recordPage(request, loginSession);
	}

	private boolean shouldSkip(HttpServletRequest request, Object handler) {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		String uri = request.getRequestURI();
		if (uri == null) {
			return true;
		}
		if (uri.startsWith("/api/")
				|| uri.startsWith("/auth/")
				|| uri.startsWith("/assets/")
				|| uri.startsWith("/vendors/")
				|| uri.startsWith("/error")
				|| uri.contains("/favicon")) {
			return true;
		}
		String accept = request.getHeader("Accept");
		if (accept != null && accept.contains("application/json") && !accept.contains("text/html")) {
			return true;
		}
		return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
	}
}
