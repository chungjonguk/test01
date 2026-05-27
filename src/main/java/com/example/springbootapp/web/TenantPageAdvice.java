package com.example.springbootapp.web;

import com.example.springbootapp.config.web.CompanyHostTenantContext;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.service.CompanyTenantContext;
import com.example.springbootapp.service.DashboardCompanySessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 업체 도메인(고객 스토어프론트) 접속 시 레이아웃·API 스코프용 Model 속성.
 */
@Profile("!test")
@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
public class TenantPageAdvice {

	private static final String PRODUCT_GRID_PATH = "/app/e-commerce/product/product-grid";
	private static final String SHOP_HOME_PATH = "/shop-home";

	private final CompanyTenantContext companyTenantContext;
	private final DashboardCompanySessionService companySessionService;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public TenantPageAdvice(
			CompanyTenantContext companyTenantContext,
			DashboardCompanySessionService companySessionService,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.companyTenantContext = companyTenantContext;
		this.companySessionService = companySessionService;
		this.publicPathCrypto = publicPathCrypto;
	}

	@ModelAttribute
	public void tenantStorefront(Model model, HttpServletRequest request) {
		Long tenantCompanyId = companyTenantContext.resolveTenantCompanyId(request);
		if (tenantCompanyId == null) {
			return;
		}
		model.addAttribute("tenantResolvedHost", request.getAttribute(CompanyHostTenantContext.ATTR_RESOLVED_HOST));
		model.addAttribute("tenantCompanyId", tenantCompanyId);
		Object companyNm = request.getAttribute(CompanyHostTenantContext.ATTR_COMPANY_NAME);
		if (companyNm != null) {
			model.addAttribute("tenantCompanyName", companyNm);
		} else {
			model.addAttribute("tenantCompanyName", companySessionService.companyName(tenantCompanyId));
		}
		String servletPath = toLogicalServletPath(request);
		if (isStorefrontScreen(servletPath)) {
			model.addAttribute("storefrontMode", true);
			model.addAttribute("hideSidebar", true);
			model.addAttribute("loadProductGridActions", true);
			model.addAttribute("loadProductCartActions", true);
		}
	}

	private boolean isStorefrontScreen(String path) {
		if (path == null) {
			return false;
		}
		String p = DoPathHelper.stripDoSuffix(path);
		return SHOP_HOME_PATH.equals(p)
				|| PRODUCT_GRID_PATH.equals(p)
				|| p.startsWith(PRODUCT_GRID_PATH)
				|| p.contains("/app/e-commerce/product/product-details")
				|| p.contains("/app/e-commerce/shopping-cart");
	}

	private String toLogicalServletPath(HttpServletRequest request) {
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			return crypto.resolveLogicalPath(request);
		}
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
	}
}
