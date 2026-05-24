package com.example.springbootapp.web;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.springbootapp.config.InicisProperties;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.service.ScreenListService;
import jakarta.servlet.http.HttpServletRequest;
/**
 * MVC 페이지 공통 Model 주입: 화면ID, 제목, 메뉴 URL, 스크립트 플래그.
 */
@Profile("!test")
@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
public class PageViewAdvice {
	private static final String KAKAO_KEY_PLACEHOLDER = "YOUR_KAKAO_REST_API_KEY";
	private final ScreenListService screenListService;
	private final InicisProperties inicisProperties;
	@Value("${kakao.javascript-key:}")
	private String kakaoJavascriptKey;
	@Value("${kakao.client-id:}")
	private String kakaoClientId;
	@Value("${app.brand-name:PrintMall}")
	private String appBrandName;
	public PageViewAdvice(ScreenListService screenListService, InicisProperties inicisProperties) {
		this.screenListService = screenListService;
		this.inicisProperties = inicisProperties;
	}
	@ModelAttribute("activeScreenUris")
	public List<String> activeScreenUris() {
		return screenListService.findActiveUriPaths();
	}
	@ModelAttribute("appBrandName")
	public String appBrandName() {
		return appBrandName;
	}
	@ModelAttribute
	public void applyPageViewAttributes(HttpServletRequest request, Model model) {
		if (request == null) {
			return;
		}
		String uri = request.getRequestURI();
		if (uri == null || shouldSkip(uri)) {
			return;
		}
		applyScreenAttributes(uri, model);
		applyScriptFlags(uri, model);
	}
	private void applyScreenAttributes(String uri, Model model) {
		ScreenList screen = screenListService.resolveForRequest(uri);
		if (screen == null || !"Y".equalsIgnoreCase(screen.getUseYn())) {
			return;
		}
		setIfAbsent(model, "screenId", screen.getScreenId());
		setIfAbsent(model, "screenNm", screen.getScreenNm());
		setIfAbsent(model, "title", screen.getScreenNm());
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
		if (uri.contains("/admin/user-access-logs")) {
			setIfAbsent(model, "loadUserAccessLogActions", true);
		}
		if (uri.contains("/admin/media-storage")) {
			setIfAbsent(model, "loadMediaStorageActions", true);
		}
		if (uri.contains("/admin/companies")) {
			setIfAbsent(model, "loadCompanyManageActions", true);
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
			setIfAbsent(model, "hideSidebar", true);
			setIfAbsent(model, "loadDropzone", true);
			setIfAbsent(model, "loadWizardKakaoAddress", true);
			if (isKakaoMapKeyConfigured()) {
				setIfAbsent(model, "kakaoMapAppKey", kakaoJavascriptKey.trim());
			}
			if (StringUtils.hasText(kakaoClientId)) {
				setIfAbsent(model, "kakaoRestApiKeyForMapHint", kakaoClientId.trim());
			}
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
	private static void setIfAbsent(Model model, String name, Object value) {
		if (!model.containsAttribute(name)) {
			model.addAttribute(name, value);
		}
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
