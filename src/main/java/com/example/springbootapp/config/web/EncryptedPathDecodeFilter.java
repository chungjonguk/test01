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
 * {@code /e/{token}.do} 요청을 복호화한 논리 경로로 MVC가 처리하도록 변환합니다.
 */
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class EncryptedPathDecodeFilter extends OncePerRequestFilter {

	private final PublicPathCryptoService publicPathCryptoService;

	public EncryptedPathDecodeFilter(PublicPathCryptoService publicPathCryptoService) {
		this.publicPathCryptoService = publicPathCryptoService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!publicPathCryptoService.isEnabled()) {
			filterChain.doFilter(request, response);
			return;
		}
		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();
		String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
		if (!publicPathCryptoService.isPublicPath(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		String logical = publicPathCryptoService.toLogicalPath(path);
		if (logical.equals(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		filterChain.doFilter(new LogicalPathRequestWrapper(request, contextPath, logical), response);
	}
}
