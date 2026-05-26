package com.example.springbootapp.web;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.springbootapp.config.InicisProperties;
import com.example.springbootapp.config.ScreenSidebarLoader;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.service.ScreenListService;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
/**
 * MVC 페이지 공통 Model 주입: 화면ID, 제목, 메뉴 URL, 스크립트 플래그.
 */
@Profile("!test")
@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
	public class PageViewAdvice {
	private static final String KAKAO_KEY_PLACEHOLDER = "YOUR_KAKAO_REST_API_KEY";
	/** 대시보드 기본(/) — sidebar data-menu-path 와 동일 */
	public static final String DASHBOARD_HOME_MENU_KEY = "/dashboard-home";
	/** 쇼핑몰 홈(/) — sidebar data-menu-path 와 동일 */
	public static final String SHOP_HOME_MENU_KEY = "/shop-home";
	public static final String SHOP_DASHBOARD_MENU_KEY = "/shop-dashboard";
	/** sidebar-shopping-mall.html 과 동일 — 중복 URL 시 쇼핑몰 메뉴 우선 */
	private static final Set<String> SHOPPING_MALL_MENU_KEYS = Set.of(
			"/app/e-commerce/product/product-grid",
			"/app/e-commerce/product/product-list",
			"/app/e-commerce/shopping-cart",
			"/app/e-commerce/checkout",
			"/app/e-commerce/orders/order-list",
			"/app/e-commerce/orders/order-details",
			"/pages/user/profile",
			"/app/e-commerce/customers",
			"/app/e-commerce/invoice",
			"/pages/faq/faq-basic",
			"/shop-dashboard");
	private final ScreenListService screenListService;
	private final InicisProperties inicisProperties;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;
	@Value("${kakao.javascript-key:}")
	private String kakaoJavascriptKey;
	@Value("${kakao.client-id:}")
	private String kakaoClientId;
	@Value("${kakao.local.mock-enabled:true}")
	private boolean kakaoLocalMockEnabled;
	@Value("${app.brand-name:PrintMall}")
	private String appBrandName;
	@Value("${app.grid.page-size:30}")
	private int gridPageSize;
	public PageViewAdvice(
			ScreenListService screenListService,
			InicisProperties inicisProperties,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.screenListService = screenListService;
		this.inicisProperties = inicisProperties;
		this.publicPathCrypto = publicPathCrypto;
	}

	@ModelAttribute("activeScreenUris")
	public List<String> activeScreenUris() {
		return new ArrayList<>(expandActiveUriVariants(screenListService.findActiveUriPaths()));
	}

	/** 사이드바 메뉴 표시용 논리 경로 키 (암호화 URL과 무관하게 매칭) */
	@ModelAttribute("activeScreenPathKeys")
	public List<String> activeScreenPathKeys() {
		LinkedHashSet<String> keys = new LinkedHashSet<>();
		for (String path : screenListService.findActiveUriPaths()) {
			if (path == null || path.isBlank()) {
				continue;
			}
			keys.add(logicalPathKey(screenListService.normalizeUriPath(path)));
			keys.add(logicalPathKey(path));
		}
		keys.add(DASHBOARD_HOME_MENU_KEY);
		keys.add(SHOP_HOME_MENU_KEY);
		keys.add(SHOP_DASHBOARD_MENU_KEY);
		keys.addAll(SHOPPING_MALL_MENU_KEYS);
		return new ArrayList<>(keys);
	}

	private LinkedHashSet<String> expandActiveUriVariants(List<String> paths) {
		LinkedHashSet<String> expanded = new LinkedHashSet<>();
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		for (String path : paths) {
			if (path == null || path.isBlank()) {
				continue;
			}
			String normalized = screenListService.normalizeUriPath(path);
			String stripped = DoPathHelper.stripDoSuffix(path);
			String strippedNorm = DoPathHelper.stripDoSuffix(normalized);
			expanded.add(path);
			expanded.add(normalized);
			expanded.add(stripped);
			expanded.add(strippedNorm);
			expanded.add(logicalPathKey(path));
			expanded.add(logicalPathKey(normalized));
			if (crypto != null && crypto.isEnabled()) {
				expanded.add(crypto.toPublicPath(path));
				expanded.add(crypto.toPublicPath(normalized));
				expanded.add(crypto.toPublicPath(stripped));
			}
		}
		return expanded;
	}

	private static String logicalPathKey(String path) {
		return DoPathHelper.stripDoSuffix(path);
	}

	private static boolean isDashboardEcommerceScreenId(String screenId) {
		return screenId != null
				&& screenId.startsWith("DASHBOARD_")
				&& screenId.contains("ECOMMERCE");
	}

	private static String resolveMenuZone(String pathKey, ScreenList screen) {
		if (screen != null && screen.getScreenId() != null) {
			String screenId = screen.getScreenId();
			if (screenId.startsWith("SHOP_")) {
				return "shopping-mall";
			}
			if (screenId.startsWith("ADMIN_")) {
				return "admin";
			}
			if ("HOME".equals(screenId) || screenId.startsWith("DASHBOARD_") || isDashboardEcommerceScreenId(screenId)) {
				return "dashboard";
			}
		}
		if (pathKey == null || pathKey.isBlank()) {
			return "";
		}
		if (pathKey.startsWith("/admin")) {
			return "admin";
		}
		if (SHOPPING_MALL_MENU_KEYS.contains(pathKey)) {
			return "shopping-mall";
		}
		if (pathKey.startsWith("/dashboard")) {
			return "dashboard";
		}
		if (pathKey.startsWith("/app/") || pathKey.startsWith("/pages/")) {
			return "app";
		}
		return "";
	}
	@ModelAttribute("appBrandName")
	public String appBrandName() {
		return appBrandName;
	}
	@ModelAttribute("gridPageSize")
	public int gridPageSize() {
		return gridPageSize > 0 ? gridPageSize : 30;
	}
	@ModelAttribute
	public void applyPageViewAttributes(HttpServletRequest request, Model model) {
		if (request == null) {
			return;
		}
		String uri = resolveRequestUri(request);
		if (uri == null || shouldSkip(uri)) {
			return;
		}
		String pathKey = logicalPathKey(screenListService.normalizeUriPath(uri));
		ScreenList screen = screenListService.resolveForRequest(uri);
		String menuPathKey = resolveMenuPathKey(pathKey, screen);
		String menuZone = resolveMenuZone(pathKey, screen);
		setIfAbsent(model, "currentScreenPathKey", menuPathKey);
		setIfAbsent(model, "currentMenuZone", menuZone);
		applyScreenAttributes(uri, model);
		ScreenList resolved = screenListService.resolveForRequest(uri);
		String finalPathKey = (String) model.getAttribute("currentScreenPathKey");
		String finalZone = (String) model.getAttribute("currentMenuZone");
		applyMenuPresentation(model, resolved, finalPathKey, finalZone);
		applyScriptFlags(uri, model);
	}
	private void applyScreenAttributes(String uri, Model model) {
		ScreenList screen = screenListService.resolveForRequest(uri);
		if (screen == null || !"Y".equalsIgnoreCase(screen.getUseYn())) {
			return;
		}
		setIfAbsent(model, "screenId", screen.getScreenId());
		String displayNm = ScreenSidebarLoader.resolveDisplayName(screen.getScreenNm(), screen.getUriPath());
		setIfAbsent(model, "screenNm", displayNm);
		setIfAbsent(model, "title", displayNm);
		String uriPath = DoPathHelper.stripDoSuffix(screen.getUriPath());
		setIfAbsent(model, "screenUriPath", uriPath);
		String menuPathKey = resolveMenuPathKey(uriPath, screen);
		String menuZone = resolveMenuZone(uriPath, screen);
		model.addAttribute("currentScreenPathKey", menuPathKey);
		model.addAttribute("currentMenuZone", menuZone);
	}

	private void applyMenuPresentation(Model model, ScreenList screen, String menuPathKey, String menuZone) {
		model.addAttribute("menuBreadcrumb", buildMenuBreadcrumb(screen, menuPathKey, menuZone));
	}

	private static List<MenuBreadcrumbItem> buildMenuBreadcrumb(ScreenList screen, String menuPathKey, String menuZone) {
		List<MenuBreadcrumbItem> crumbs = new ArrayList<>();
		if (menuZone != null && !menuZone.isBlank()) {
			crumbs.add(new MenuBreadcrumbItem(zoneLabel(menuZone)));
		}
		crumbs.add(new MenuBreadcrumbItem(pageLabel(screen, menuPathKey)));
		return crumbs;
	}

	private static String zoneLabel(String zone) {
		return switch (zone) {
			case "dashboard" -> "대시보드";
			case "shopping-mall" -> "쇼핑몰";
			case "admin" -> "관리";
			case "app" -> "앱";
			default -> zone;
		};
	}

	private static String pageLabel(ScreenList screen, String menuPathKey) {
		if (DASHBOARD_HOME_MENU_KEY.equals(menuPathKey)) {
			return "기본";
		}
		if (SHOP_HOME_MENU_KEY.equals(menuPathKey)) {
			return "쇼핑몰 홈";
		}
		if (SHOP_DASHBOARD_MENU_KEY.equals(menuPathKey)) {
			return "쇼핑몰 통계";
		}
		if ("/dashboard/e-commerce".equals(menuPathKey)) {
			return "이커머스";
		}
		if (screen != null) {
			return ScreenSidebarLoader.resolveDisplayName(screen.getScreenNm(), screen.getUriPath());
		}
		return menuPathKey != null && !menuPathKey.isBlank() ? menuPathKey : "현재 화면";
	}

	private static String resolveMenuPathKey(String pathKey, ScreenList screen) {
		if (screen != null && screen.getScreenId() != null) {
			if ("SHOP_HOME".equals(screen.getScreenId())) {
				return SHOP_HOME_MENU_KEY;
			}
			if ("SHOP_DASHBOARD".equals(screen.getScreenId())) {
				return SHOP_DASHBOARD_MENU_KEY;
			}
			if ("DASHBOARD_ECOMMERCE".equals(screen.getScreenId())
					|| isDashboardEcommerceScreenId(screen.getScreenId())) {
				return "/dashboard/e-commerce";
			}
			if ("HOME".equals(screen.getScreenId()) && "/".equals(pathKey)) {
				return DASHBOARD_HOME_MENU_KEY;
			}
		}
		return pathKey;
	}
	private void applyScriptFlags(String uri, Model model) {
		if (uri.contains("/modules/charts/echarts")) {
			setIfAbsent(model, "loadEchartsExamples", true);
		}
		if (uri.contains("/modules/charts/d3js") || uri.contains("/dashboard/lms")) {
			setIfAbsent(model, "loadD3", true);
		}
		if (uri.contains("/dashboard/analytics")) {
			setIfAbsent(model, "loadDayjs", true);
			setIfAbsent(model, "loadCountUp", true);
			setIfAbsent(model, "loadWorldMap", true);
		}
		if (uri.contains("/dashboard/crm")) {
			setIfAbsent(model, "loadDayjs", true);
			setIfAbsent(model, "loadWorldMap", true);
			setIfAbsent(model, "loadCrmDashboardInit", true);
			model.addAttribute("loadCalendar", false);
			model.addAttribute("loadFlatpickr", false);
		}
		if (uri.contains("/dashboard/lms")) {
			setIfAbsent(model, "loadChartJs", true);
			setIfAbsent(model, "loadCountUp", true);
			setIfAbsent(model, "loadWorldMap", true);
		}
		if (uri.contains("/dashboard/saas") || uri.contains("/dashboard/e-commerce")) {
			setIfAbsent(model, "loadCountUp", true);
			setIfAbsent(model, "loadDayjs", true);
		}
		if (uri.contains("/dashboard/project-management")) {
			setIfAbsent(model, "loadCountUp", true);
			setIfAbsent(model, "loadDayjs", true);
			setIfAbsent(model, "loadLeaflet", true);
			setIfAbsent(model, "loadCalendar", true);
		}
		if (uri.contains("/widgets")) {
			setIfAbsent(model, "loadChartJs", true);
			setIfAbsent(model, "loadCountUp", true);
			setIfAbsent(model, "loadDayjs", true);
			setIfAbsent(model, "loadWorldMap", true);
			setIfAbsent(model, "loadLeaflet", true);
			setIfAbsent(model, "loadCalendar", true);
			setIfAbsent(model, "loadD3", true);
			setIfAbsent(model, "loadTinymce", true);
			setIfAbsent(model, "loadDropzone", true);
		}
		if (uri.contains("/modules/maps/leaflet")) {
			setIfAbsent(model, "loadLeaflet", true);
		}
		if (uri.contains("/modules/charts/chartjs")) {
			setIfAbsent(model, "loadChartJs", true);
		}
		if (uri.contains("/app/e-commerce/product") || uri.contains("/modules/components/carousel")) {
			setIfAbsent(model, "loadSwiper", true);
		}
		if (uri.contains("/app/e-commerce/product/product-register")) {
			setIfAbsent(model, "loadProductRegisterActions", true);
		}
		if (uri.contains("/app/e-commerce/product/product-manage")) {
			setIfAbsent(model, "loadProductManageActions", true);
			setIfAbsent(model, "loadSwiper", true);
		}
		if (uri.contains("/app/e-commerce/product/product-details")) {
			setIfAbsent(model, "loadProductDetailsActions", true);
			setIfAbsent(model, "loadSwiper", true);
		}
		if (uri.contains("/app/e-commerce/product/product-images")) {
			setIfAbsent(model, "loadProductImagesPage", true);
		}
		if (uri.contains("/app/e-commerce/orders/order-list")) {
			setIfAbsent(model, "loadOrderListActions", true);
		}
		if (uri.contains("/app/e-commerce/customer-details")) {
			setIfAbsent(model, "loadCustomerDetailsActions", true);
		}
		if (uri.contains("/app/e-commerce/billing")) {
			setIfAbsent(model, "loadBillingActions", true);
		}
		if (uri.contains("/app/e-commerce/checkout") && !uri.contains("/inicis/")) {
			setIfAbsent(model, "loadCheckoutActions", true);
			setIfAbsent(model, "inicisMockEnabled", inicisProperties.isMockEnabled());
			setIfAbsent(model, "inicisUseRealGateway", inicisProperties.useRealGateway());
			setIfAbsent(model, "inicisMid", inicisProperties.getMid());
		}
		if (uri.contains("/app/e-commerce/customers")) {
			setIfAbsent(model, "loadCustomersActions", true);
		}
		if (uri.contains("/app/e-commerce/shopping-cart")) {
			setIfAbsent(model, "loadShoppingCartActions", true);
		}
		if (uri.contains("/app/e-commerce/invoice")) {
			setIfAbsent(model, "loadInvoicePdf", true);
		}
		if (uri.contains("/admin/codes")) {
			setIfAbsent(model, "loadCodeManagementActions", true);
		}
		if (uri.contains("/admin/menus")) {
			setIfAbsent(model, "loadMenuManagementActions", true);
		}
		if (uri.contains("/admin/dashboard-config")) {
			setIfAbsent(model, "loadDashboardConfigActions", true);
		}
		if (uri.contains("/admin/user-access-logs")) {
			setIfAbsent(model, "loadUserAccessLogActions", true);
		}
		if (uri.contains("/admin/media-storage")) {
			setIfAbsent(model, "loadMediaStorageActions", true);
			setIfAbsent(model, "loadNasAdminActions", true);
		}
		if (uri.contains("/admin/nas")) {
			setIfAbsent(model, "loadNasAdminActions", true);
		}
		if (uri.contains("/admin/companies")) {
			setIfAbsent(model, "loadCompanyManageActions", true);
			applyKakaoAddressFormFlags(model);
		}
		if (uri.contains("/admin/inventory")) {
			setIfAbsent(model, "loadInventoryManageActions", true);
		}
		if (uri.contains("/admin/shipping")) {
			setIfAbsent(model, "loadShippingWaybillActions", true);
		}
		if (uri.contains("/admin/company-page-images")) {
			setIfAbsent(model, "loadCompanyPageImageActions", true);
		}
		if (uri.contains("/app/email/email-detail")) {
			setIfAbsent(model, "loadEmailDetailActions", true);
		}
		if (uri.contains("/app/email/inbox")) {
			setIfAbsent(model, "loadEmailInboxActions", true);
		}
		if (uri.contains("/app/email/compose")
				|| uri.contains("/modules/forms/advance/editor")
				|| uri.contains("/app/e-learning/course/create-a-course")) {
			setIfAbsent(model, "loadTinymce", true);
		}
		if (uri.contains("/app/email/compose")) {
			setIfAbsent(model, "loadEmailComposeActions", true);
		}
		if (uri.contains("/app/calendar") || uri.contains("/modules/components/calendar")) {
			setIfAbsent(model, "loadCalendar", true);
		}
		if (uri.contains("/app/events/create-an-event")) {
			setIfAbsent(model, "loadFlatpickr", true);
		}
		if (uri.contains("/app/calendar")) {
			setIfAbsent(model, "loadCalendarActions", true);
		}
		if (uri.contains("/modules/forms/advance/file-uploader")) {
			setIfAbsent(model, "loadDropzone", true);
		}
		if (uri.contains("/app/kanban")) {
			setIfAbsent(model, "hideSidebar", true);
		}
		if (uri.contains("/pages/authentication/")) {
			setIfAbsent(model, "hideSidebar", true);
		}
		if (uri.contains("/pages/authentication/wizard")
				|| uri.contains("/modules/components/animated-icons")
				|| uri.contains("/pages/errors/")) {
			setIfAbsent(model, "loadLottie", true);
		}
		if (uri.contains("/modules/forms/wizard")) {
			setIfAbsent(model, "loadLottie", true);
		}
		if (uri.contains("/pages/authentication/wizard")) {
			applyKakaoAddressFormFlags(model);
			setIfAbsent(model, "loadWizardAddressInit", true);
		}
		if ("/users".equals(DoPathHelper.stripDoSuffix(uri))) {
			applyKakaoAddressFormFlags(model);
			setIfAbsent(model, "loadUsersAddressInit", true);
		}
		if (uri.contains("/pages/authentication/wizard")) {
			setIfAbsent(model, "hideSidebar", true);
			setIfAbsent(model, "loadDropzone", true);
		}
		applyScriptFlagDefaults(model);
	}
	private boolean isKakaoRestApiConfigured() {
		if (!StringUtils.hasText(kakaoClientId)) {
			return false;
		}
		String key = kakaoClientId.trim();
		return !KAKAO_KEY_PLACEHOLDER.equals(key) && !key.startsWith("여기에_");
	}

	private void applyKakaoAddressFormFlags(Model model) {
		setIfAbsent(model, "loadKakaoAddressForm", true);
		setIfAbsent(model, "kakaoAddressConfigured", isKakaoRestApiConfigured());
		setIfAbsent(model, "kakaoAddressUsesMock", kakaoLocalMockEnabled && !isKakaoRestApiConfigured());
		if (isKakaoMapKeyConfigured()) {
			setIfAbsent(model, "kakaoMapAppKey", kakaoJavascriptKey.trim());
		}
		if (isKakaoRestApiConfigured()) {
			setIfAbsent(model, "kakaoRestApiKeyForMapHint", kakaoClientId.trim());
		}
	}

	private boolean isKakaoMapKeyConfigured() {
		if (!StringUtils.hasText(kakaoJavascriptKey)) {
			return false;
		}
		String jsKey = kakaoJavascriptKey.trim();
		if (KAKAO_KEY_PLACEHOLDER.equals(jsKey)) {
			return false;
		}
		// REST API 키를 JavaScript 키 자리에 넣으면 카카오맵 SDK가 로드되지 않음
		return !StringUtils.hasText(kakaoClientId) || !jsKey.equals(kakaoClientId.trim());
	}
	/** layout.html SpEL(or) — null 대신 false 로 통일 */
	private static void applyScriptFlagDefaults(Model model) {
		setIfAbsent(model, "loadCalendar", false);
		setIfAbsent(model, "loadFlatpickr", false);
		setIfAbsent(model, "loadDayjs", false);
		setIfAbsent(model, "loadEchartsExamples", false);
		setIfAbsent(model, "loadWorldMap", false);
		setIfAbsent(model, "loadCrmDashboardInit", false);
	}

	private static void setIfAbsent(Model model, String name, Object value) {
		if (!model.containsAttribute(name)) {
			model.addAttribute(name, value);
		}
	}
	private String resolveRequestUri(HttpServletRequest request) {
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			return crypto.resolveLogicalPath(request);
		}
		return request.getRequestURI();
	}

	private boolean shouldSkip(String uri) {
		return uri.startsWith("/api/")
				|| uri.startsWith("/auth/")
				|| uri.startsWith("/error")
				|| uri.contains("/favicon")
				|| uri.startsWith("/assets/")
				|| uri.startsWith("/vendors/");
	}
}
