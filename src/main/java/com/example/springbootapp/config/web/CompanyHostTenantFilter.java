package com.example.springbootapp.config.web;

import com.example.springbootapp.service.CompanyHostTenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 등록된 업체 도메인(Host)으로 접속 시 세션 선택 업체를 자동 설정한다.
 */
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CompanyHostTenantFilter extends OncePerRequestFilter {

	private final CompanyHostTenantService companyHostTenantService;

	public CompanyHostTenantFilter(CompanyHostTenantService companyHostTenantService) {
		this.companyHostTenantService = companyHostTenantService;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path == null) {
			return false;
		}
		return path.startsWith("/assets/")
				|| path.startsWith("/vendors/")
				|| path.startsWith("/webjars/")
				|| path.startsWith("/favicon");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		companyHostTenantService.applyTenantFromHost(request);
		filterChain.doFilter(request, response);
	}
}
