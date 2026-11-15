package com.example.springbootapp.config;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.domain.ScreenList;
/**
 * screen_list 시드: 사이드바 전체 메뉴 + 사이드바에 없는 보조 화면.
 */
public final class ScreenCatalog {
	private ScreenCatalog() {
	}
	public static List<ScreenList> all() {
		Map<String, ScreenList> byUri = new LinkedHashMap<>();
		for (ScreenList fromSidebar : ScreenSidebarLoader.fromSidebar()) {
			byUri.put(fromSidebar.getUriPath(), fromSidebar);
		}
		addSupplemental(byUri);
		return new ArrayList<>(byUri.values());
	}
	/** 사이드바에 링크는 없으나 앱에서 사용하는 화면 */
	private static void addSupplemental(Map<String, ScreenList> byUri) {
		put(byUri, "DASHBOARD", "대시보드", "/dashboard", "index", 12);
		put(byUri, "APP_COURSE_LIST", "강좌 목록", "/app/e-learning/course/course-list",
				"app/e-learning/course/course-list", 355);
		put(byUri, "APP_COURSE_GRID", "강좌 그리드", "/app/e-learning/course/course-grid",
				"app/e-learning/course/course-grid", 356);
		put(byUri, "APP_COURSE_DETAIL", "강좌 상세", "/app/e-learning/course/course-details",
				"app/e-learning/course/course-details", 357);
		put(byUri, "APP_COURSE_CREATE", "강좌 만들기", "/app/e-learning/course/create-a-course",
				"app/e-learning/course/create-a-course", 358);
		put(byUri, "ECM_PRODUCT_REGISTER", "상품 등록", "/app/e-commerce/product/product-register",
				"app/e-commerce/product/product-register", 168);
		put(byUri, "ECM_PRODUCT_MANAGE", "상품 관리", "/app/e-commerce/product/product-manage",
				"app/e-commerce/product/product-manage", 169);
		put(byUri, "ECM_PRODUCT_IMAGES", "상품 이미지", "/app/e-commerce/product/product-images",
				"app/e-commerce/product/product-images", 170);
		put(byUri, "ADMIN_TABLE_SEQUENCES", "테이블 시퀀스", "/admin/table-sequences", "admin/table-sequences", 24);
		put(byUri, "ADMIN_MENUS", "메뉴관리", "/admin/menus", "admin/menus", 25);
		put(byUri, "ADMIN_USER_ACCESS_LOGS", "접속 로그", "/admin/user-access-logs", "admin/user-access-logs", 26);
		put(byUri, "ADMIN_COMPANIES", "업체관리", "/admin/companies", "admin/companies", 27);
		put(byUri, "ADMIN_NAS", "NAS 설정", "/admin/nas", "admin/nas-settings", 28);
		put(byUri, "ADMIN_NAS_FILES", "NAS 파일", "/admin/media-storage", "admin/media-storage", 29);
		put(byUri, "ADMIN_DASHBOARD_CONFIG", "대시보드 구성", "/admin/dashboard-config", "admin/dashboard-config", 30);
		put(byUri, "ADMIN_COMPANY_PAGE_IMAGES", "페이지 이미지", "/admin/company-page-images", "admin/company-page-images", 31);
		put(byUri, "ADMIN_COMPANY_DOMAINS", "업체 도메인", "/admin/company-domains", "admin/company-domains", 32);
		put(byUri, "ADMIN_INVENTORY", "재고관리", "/admin/inventory", "admin/inventory", 33);
		put(byUri, "ADMIN_SHIPPING", "운송장발급", "/admin/shipping", "admin/shipping", 34);
		put(byUri, "SHOP_HOME", "쇼핑몰 홈", "/shop-home", "app/e-commerce/product/product-grid", 15);
		put(byUri, "SHOP_DASHBOARD", "쇼핑몰 통계", "/shop-dashboard", "dashboard/e-commerce", 26);
		// React PrintMall 관리·보조 화면
		put(byUri, "ADMIN_SCREEN_LOCALES", "화면별 언어", "/admin/screen-locales", "admin/screen-locales", 735, "관리자", "fas fa-language");
		put(byUri, "ADMIN_LAYOUT", "레이아웃 구조", "/admin/layout", "admin/layout", 736, "관리자", "fas fa-border-all");
		put(byUri, "ADMIN_WIDGETS", "위젯 등록", "/admin/widgets", "admin/widgets", 737, "관리자", "fas fa-puzzle-piece");
		put(byUri, "ADMIN_WIDGET_LAYOUTS", "위젯 레이아웃", "/admin/widget-layouts", "admin/widget-layouts", 738, "관리자", "fas fa-th-large");
		put(byUri, "ADMIN_PERMISSIONS", "화면 권한", "/admin/permissions", "admin/permissions", 739, "관리자", "fas fa-user-shield");
		put(byUri, "PAGES_WIDGET_BOARD", "위젯 보드", "/pages/widget-board", "pages/widget-board", 371, "페이지", "fas fa-th");
		put(byUri, "PAGES_ERRORS_403", "403", "/pages/errors/403", "pages/errors/403", 615, "페이지", "fas fa-ban");
		put(byUri, "PAGES_AUTHENTICATION_MODAL", "Modal", "/pages/authentication/modal", "pages/authentication/modal", 605, "페이지", "fas fa-window-maximize");
		put(byUri, "PAGES_USER_PROFILE", "프로필", "/pages/user/profile", "pages/user/profile", 612, "마이페이지", "fas fa-user");
		put(byUri, "AUTH_LOGIN_SHORT", "로그인(단축)", "/login", "pages/authentication/simple/login", 385, "페이지", "fas fa-sign-in-alt");
		put(byUri, "AUTH_REGISTER_SHORT", "회원가입(단축)", "/register", "pages/authentication/simple/register", 386, "페이지", "fas fa-user-plus");
		put(byUri, "AUTH_FORGOT_SHORT", "비밀번호 찾기(단축)", "/forgot-password", "pages/authentication/simple/forgot-password", 387, "페이지", "fas fa-key");
	}
	private static void put(Map<String, ScreenList> byUri, String id, String nm, String uri, String template, int sort) {
		put(byUri, id, nm, uri, template, sort, null, null);
	}
	private static void put(
			Map<String, ScreenList> byUri,
			String id,
			String nm,
			String uri,
			String template,
			int sort,
			String menuGroup,
			String iconClass) {
		String normalized = DoPathHelper.normalizeForScreenLookup(uri);
		if (byUri.containsKey(normalized)) {
			ScreenList existing = byUri.get(normalized);
			if (shouldKeepExistingScreen(existing.getScreenId(), id)) {
				return;
			}
			existing.setScreenNm(nm);
			existing.setScreenId(id);
			if (menuGroup != null) {
				existing.setMenuGroup(menuGroup);
			}
			if (iconClass != null) {
				existing.setIconClass(iconClass);
			}
			return;
		}
		ScreenList s = new ScreenList();
		s.setScreenId(id);
		s.setScreenNm(nm);
		s.setUriPath(normalized);
		s.setTemplatePath(template);
		s.setSortOrd(sort);
		s.setUseYn("Y");
		s.setMenuGroup(menuGroup);
		s.setIconClass(iconClass);
		byUri.put(normalized, s);
	}

	/** 사이드바·앱·대시보드 화면을 쇼핑몰 보조 ID가 같은 URI로 덮어쓰지 않음 */
	private static boolean shouldKeepExistingScreen(String existingId, String newId) {
		if (newId == null || !newId.startsWith("SHOP_")) {
			return false;
		}
		if (existingId == null || existingId.isBlank()) {
			return false;
		}
		return existingId.startsWith("APP_")
				|| existingId.startsWith("DASHBOARD_")
				|| existingId.startsWith("ECM_")
				|| existingId.startsWith("ADMIN_")
				|| "HOME".equals(existingId);
	}
}
