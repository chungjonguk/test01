package com.example.springbootapp.service;

import com.example.springbootapp.config.TenantHostProperties;
import com.example.springbootapp.config.web.CompanyHostTenantContext;
import com.example.springbootapp.domain.BizCompanyDomain;
import com.example.springbootapp.mapper.BizCompanyDomainMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Host 테넌트·세션 선택 업체를 기준으로 API/화면의 업체 스코프를 해석합니다.
 */
@Service
public class CompanyTenantContext {

	public static final String STORE_CATALOG_STATUS = "ACTIVE";

	private final TenantHostProperties tenantHostProperties;
	private final BizCompanyDomainMapper bizCompanyDomainMapper;
	private final DashboardCompanySessionService companySessionService;

	public CompanyTenantContext(
			TenantHostProperties tenantHostProperties,
			BizCompanyDomainMapper bizCompanyDomainMapper,
			DashboardCompanySessionService companySessionService) {
		this.tenantHostProperties = tenantHostProperties;
		this.bizCompanyDomainMapper = bizCompanyDomainMapper;
		this.companySessionService = companySessionService;
	}

	public boolean isStorefrontRequest(HttpServletRequest request) {
		return resolveTenantCompanyId(request) != null;
	}

	public Long resolveTenantCompanyId(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Object attr = request.getAttribute(CompanyHostTenantContext.ATTR_COMPANY_ID);
		if (attr instanceof Number number) {
			return number.longValue();
		}
		if (!tenantHostProperties.isEnabled()) {
			return null;
		}
		String host = CompanyHostTenantService.resolveRequestHost(request);
		if (host == null || isIgnoredHost(host)) {
			return null;
		}
		BizCompanyDomain domain = bizCompanyDomainMapper.findActiveByHostName(host);
		return domain != null ? domain.getCompanyId() : null;
	}

	/**
	 * 상품 조회·저장용 업체 ID. Host 테넌트가 있으면 우선, 없으면 대시보드 선택 업체.
	 */
	public Long resolveProductScopeCompanyId(HttpServletRequest request, HttpSession session) {
		Long tenantId = resolveTenantCompanyId(request);
		if (tenantId != null) {
			return tenantId;
		}
		return companySessionService.resolveSelectedCompanyId(session);
	}

	private boolean isIgnoredHost(String host) {
		String normalized = host.toLowerCase();
		for (String ignored : tenantHostProperties.getIgnoreHosts()) {
			if (ignored != null && normalized.equals(ignored.trim().toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
