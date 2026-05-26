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
	}
	private static void put(Map<String, ScreenList> byUri, String id, String nm, String uri, String template, int sort) {
		String normalized = DoPathHelper.normalizeForScreenLookup(uri);
		if (byUri.containsKey(normalized)) {
			ScreenList existing = byUri.get(normalized);
			if (shouldKeepExistingScreen(existing.getScreenId(), id)) {
				return;
			}
			existing.setScreenNm(nm);
			existing.setScreenId(id);
			return;
		}
		ScreenList s = new ScreenList();
		s.setScreenId(id);
		s.setScreenNm(nm);
		s.setUriPath(normalized);
		s.setTemplatePath(template);
		s.setSortOrd(sort);
		s.setUseYn("Y");
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
