package com.example.springbootapp.web;

import com.example.springbootapp.config.DashboardWidgetCatalog;
import com.example.springbootapp.config.web.CompanyHostTenantContext;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.service.DashboardCompanyConfigService;
import com.example.springbootapp.service.DashboardCompanySessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 사이드바·대시보드 화면에 업체 목록·선택 업체·구성 데이터를 노출한다.
 */
@ControllerAdvice
public class DashboardPageAdvice {

	private final DashboardCompanySessionService companySessionService;
	private final DashboardCompanyConfigService configService;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public DashboardPageAdvice(
			DashboardCompanySessionService companySessionService,
			DashboardCompanyConfigService configService,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.companySessionService = companySessionService;
		this.configService = configService;
		this.publicPathCrypto = publicPathCrypto;
	}

	@ModelAttribute
	public void dashboardCompanyContext(Model model, HttpSession session, HttpServletRequest request) {
		var companies = companySessionService.listActiveCompanies();
		model.addAttribute("dashboardCompanies", companies);
		Long companyId = companySessionService.resolveSelectedCompanyId(session);
		model.addAttribute("dashboardCompanyId", companyId);
		model.addAttribute("dashboardCompanyName", companySessionService.companyName(companyId));

		Object tenantHost = request.getAttribute(CompanyHostTenantContext.ATTR_RESOLVED_HOST);
		if (tenantHost != null) {
			model.addAttribute("tenantResolvedHost", tenantHost);
			model.addAttribute("tenantCompanyName", request.getAttribute(CompanyHostTenantContext.ATTR_COMPANY_NAME));
		}

		if (!isDashboardUri(request)) {
			return;
		}
		model.addAttribute("dashboardWidgetCatalog", DashboardWidgetCatalog.toMaps(DashboardWidgetCatalog.all()));
		model.addAttribute("dashboardDefaultIds", DashboardWidgetCatalog.defaultEnabledIds());
		if (companyId != null) {
			model.addAttribute("dashboardLayoutConfig", configService.getConfig(companyId));
		} else {
			model.addAttribute("dashboardLayoutConfig", configService.getDefaultConfig());
		}
	}

	private boolean isDashboardUri(HttpServletRequest request) {
		String path = toLogicalServletPath(request);
		return "/".equals(path)
				|| "/index".equals(path)
				|| "/index.do".equals(path)
				|| "/dashboard".equals(path)
				|| "/dashboard.do".equals(path)
				|| path.startsWith("/admin/dashboard-config");
	}

	private String toLogicalServletPath(HttpServletRequest request) {
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			return crypto.resolveLogicalPath(request);
		}
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String servletPath = uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
		return DoPathHelper.stripDoSuffix(servletPath);
	}
}
