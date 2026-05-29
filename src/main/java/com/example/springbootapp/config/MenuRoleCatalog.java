package com.example.springbootapp.config;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 역할별 기본 허용 메뉴 경로 (논리 path, data-menu-path 기준).
 */
public final class MenuRoleCatalog {

	/** 플랫폼 관리자 전용 (업체 관리자·고객 제외) */
	public static final Set<String> PLATFORM_ONLY_PATHS = Set.of(
			"/users",
			"/admin/codes",
			"/admin/menus",
			"/admin/table-sequences",
			"/admin/user-access-logs",
			"/admin/nas");

	public static final Set<String> COMPANY_ADMIN_PATHS = Set.of(
			"/",
			"/dashboard",
			"/dashboard-home",
			"/shop-home",
			"/shop-dashboard",
			"/admin/companies",
			"/admin/company-domains",
			"/admin/company-page-images",
			"/admin/company-customer-menus",
			"/admin/dashboard-config",
			"/admin/inventory",
			"/admin/shipping",
			"/admin/media-storage",
			"/app/e-commerce/product/product-grid",
			"/app/e-commerce/product/product-list",
			"/app/e-commerce/product/product-register",
			"/app/e-commerce/product/product-manage",
			"/app/e-commerce/product/product-images",
			"/app/e-commerce/product/product-details",
			"/app/e-commerce/billing",
			"/app/e-commerce/shopping-cart",
			"/app/e-commerce/checkout",
			"/app/e-commerce/orders/order-list",
			"/app/e-commerce/orders/order-details",
			"/app/e-commerce/customers",
			"/app/e-commerce/invoice",
			"/pages/user/settings");

	/** 업체 고객 메뉴 미설정 시 기본 노출 */
	public static final Set<String> DEFAULT_CUSTOMER_MENU_PATHS = Set.of(
			"/shop-home",
			"/app/e-commerce/product/product-grid",
			"/app/e-commerce/product/product-details",
			"/app/e-commerce/shopping-cart",
			"/app/e-commerce/checkout");

	/** 고객 메뉴 설정 화면에서 선택 가능한 후보 */
	public static final Set<String> CUSTOMER_MENU_CANDIDATES = new LinkedHashSet<>();

	static {
		CUSTOMER_MENU_CANDIDATES.addAll(DEFAULT_CUSTOMER_MENU_PATHS);
		CUSTOMER_MENU_CANDIDATES.add("/pages/user/settings");
	}

	private MenuRoleCatalog() {
	}
}
