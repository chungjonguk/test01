package com.example.springbootapp.config.web;

import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

/**
 * 암호화 URL 요청의 논리 경로를 요청 속성에 담습니다.
 * <p>DispatcherServlet 매칭은 등록된 {@code /e/{token}.do} 그대로 사용해야 합니다.</p>
 */
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class EncryptedPathDecodeFilter extends OncePerRequestFilter {

	public static final String ATTR_LOGICAL_PATH = "printmall.logicalPath";

	private final PublicPathCryptoService publicPathCryptoService;

	public EncryptedPathDecodeFilter(PublicPathCryptoService publicPathCryptoService) {
		this.publicPathCryptoService = publicPathCryptoService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (publicPathCryptoService.isEnabled()) {
			String contextPath = request.getContextPath();
			String uri = request.getRequestURI();
			String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
			if (publicPathCryptoService.isPublicPath(path)) {
				String logical = publicPathCryptoService.toLogicalPath(path);
				if (publicPathCryptoService.isPublicPath(logical)) {
					request.setAttribute(ATTR_LOGICAL_PATH, "/pages/errors/404.do");
					request.setAttribute("printmall.encryptedPathInvalid", Boolean.TRUE);
				} else {
					request.setAttribute(ATTR_LOGICAL_PATH, logical);
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
