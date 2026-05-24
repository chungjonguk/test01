package com.example.springbootapp.web;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.springbootapp.service.ScreenListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * screen_list에 등록된 화면 중 use_yn != Y 인 URL 접근을 차단합니다.
 */
@Profile("!test")
@Component
public class ScreenAccessInterceptor implements HandlerInterceptor {
	private final ScreenListService screenListService;
	public ScreenAccessInterceptor(ScreenListService screenListService) {
		this.screenListService = screenListService;
	}
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = request.getRequestURI();
		if (uri == null || shouldSkip(uri)) {
			return true;
		}
		if (screenListService.isAccessible(uri)) {
			return true;
		}
		response.sendRedirect(request.getContextPath() + "/pages/errors/404");
		return false;
	}
	private boolean shouldSkip(String uri) {
		return uri.startsWith("/api/")
				|| uri.startsWith("/auth/")
				|| uri.startsWith("/error")
				|| uri.contains("/favicon")
				|| uri.startsWith("/assets/")
				|| uri.startsWith("/vendors/")
				|| "/pages/errors/404".equals(uri)
				|| "/pages/errors/500".equals(uri);
	}
}
