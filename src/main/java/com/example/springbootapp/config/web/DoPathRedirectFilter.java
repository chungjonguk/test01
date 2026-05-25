package com.example.springbootapp.config.web;

import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 확장자 없는 페이지 GET 요청을 동일 경로의 {@code .do} URL로 리다이렉트합니다.
 */
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class DoPathRedirectFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!"GET".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}
		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();
		String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
		if (path.isEmpty()) {
			path = "/";
		}
		if (DoPathHelper.shouldSkipRedirect(path) || path.endsWith(".do")) {
			filterChain.doFilter(request, response);
			return;
		}
		String target = DoPathHelper.toDoPath(path);
		if (target.equals(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		String query = request.getQueryString();
		String location = contextPath + target + (query != null ? "?" + query : "");
		response.sendRedirect(location);
	}
}
