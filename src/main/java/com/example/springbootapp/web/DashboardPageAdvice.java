package com.example.springbootapp.web;

import com.example.springbootapp.config.DashboardWidgetCatalog;
import com.example.springbootapp.service.DashboardCompanyConfigService;
import com.example.springbootapp.service.DashboardCompanySessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

	public DashboardPageAdvice(
			DashboardCompanySessionService companySessionService,
			DashboardCompanyConfigService configService) {
		this.companySessionService = companySessionService;
		this.configService = configService;
	}

	@ModelAttribute
	public void dashboardCompanyContext(Model model, HttpSession session, HttpServletRequest request) {
		var companies = companySessionService.listActiveCompanies();
		model.addAttribute("dashboardCompanies", companies);
		Long companyId = companySessionService.resolveSelectedCompanyId(session);
		model.addAttribute("dashboardCompanyId", companyId);
		model.addAttribute("dashboardCompanyName", companySessionService.companyName(companyId));

		String uri = request.getRequestURI();
		if (uri == null || !isDashboardUri(uri)) {
			return;
		}
		model.addAttribute("dashboardWidgetCatalog", DashboardWidgetCatalog.toMaps(DashboardWidgetCatalog.all()));
		model.addAttribute("dashboardDefaultIds", DashboardWidgetCatalog.defaultEnabledIds());
		if (companyId != null) {
			model.addAttribute("dashboardLayoutConfig", configService.getConfig(companyId));
		}
	}

	private static boolean isDashboardUri(String uri) {
		return "/".equals(uri)
				|| "/dashboard".equals(uri)
				|| uri.startsWith("/admin/dashboard-config");
	}
}
