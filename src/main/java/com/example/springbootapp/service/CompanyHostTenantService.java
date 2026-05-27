package com.example.springbootapp.service;

import com.example.springbootapp.config.TenantHostProperties;
import com.example.springbootapp.config.web.CompanyHostTenantContext;
import com.example.springbootapp.domain.BizCompanyDomain;
import com.example.springbootapp.mapper.BizCompanyDomainMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * HTTP Host 헤더로 등록된 업체 도메인을 찾아 대시보드 선택 업체를 맞춘다.
 */
@Service
public class CompanyHostTenantService {

	private final TenantHostProperties tenantHostProperties;
	private final BizCompanyDomainMapper bizCompanyDomainMapper;
	private final DashboardCompanySessionService companySessionService;

	public CompanyHostTenantService(
			TenantHostProperties tenantHostProperties,
			BizCompanyDomainMapper bizCompanyDomainMapper,
			DashboardCompanySessionService companySessionService) {
		this.tenantHostProperties = tenantHostProperties;
		this.bizCompanyDomainMapper = bizCompanyDomainMapper;
		this.companySessionService = companySessionService;
	}

	/**
	 * 요청 Host에 해당하는 활성 업체가 있으면 세션·요청 속성에 반영한다.
	 */
	public void applyTenantFromHost(HttpServletRequest request) {
		if (!tenantHostProperties.isEnabled() || request == null) {
			return;
		}
		String host = resolveRequestHost(request);
		if (host == null || isIgnoredHost(host)) {
			return;
		}
		BizCompanyDomain domain = bizCompanyDomainMapper.findActiveByHostName(host);
		if (domain == null || domain.getCompanyId() == null) {
			return;
		}
		HttpSession session = request.getSession(true);
		companySessionService.setSelectedCompanyId(session, domain.getCompanyId());
		request.setAttribute(CompanyHostTenantContext.ATTR_RESOLVED_HOST, host);
		request.setAttribute(CompanyHostTenantContext.ATTR_COMPANY_ID, domain.getCompanyId());
		request.setAttribute(CompanyHostTenantContext.ATTR_COMPANY_NAME, domain.getCompanyNm());
	}

	static String resolveRequestHost(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String serverName = request.getServerName();
		if (serverName != null && !serverName.isBlank()) {
			return BizCompanyDomainService.normalizeHostName(serverName);
		}
		String hostHeader = request.getHeader("Host");
		return BizCompanyDomainService.normalizeHostName(hostHeader);
	}

	private boolean isIgnoredHost(String host) {
		String normalized = host.toLowerCase(Locale.ROOT);
		for (String ignored : tenantHostProperties.getIgnoreHosts()) {
			if (ignored == null || ignored.isBlank()) {
				continue;
			}
			if (normalized.equals(ignored.trim().toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}
}
