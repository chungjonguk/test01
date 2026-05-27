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
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * URL 세션 추적({@code ;jsessionid=...})이 경로 중간에 끼어 404가 나는 경우를 방지합니다.
 */
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class JsessionPathSanitizeFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		filterChain.doFilter(new SanitizedRequest(request), response);
	}

	private static final class SanitizedRequest extends HttpServletRequestWrapper {

		SanitizedRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getRequestURI() {
			return DoPathHelper.sanitizeRequestPath(super.getRequestURI());
		}

		@Override
		public String getServletPath() {
			return DoPathHelper.sanitizeRequestPath(super.getServletPath());
		}

		@Override
		public String getPathInfo() {
			String pathInfo = super.getPathInfo();
			return pathInfo != null ? DoPathHelper.sanitizeRequestPath(pathInfo) : null;
		}
	}
}
