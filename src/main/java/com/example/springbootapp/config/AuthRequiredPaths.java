package com.example.springbootapp.config;

import com.example.springbootapp.config.web.DoPathHelper;

/**
 * 로그인 없이 접근 가능한 경로와 인증 필수 경로를 구분합니다.
 */
public final class AuthRequiredPaths {

	private AuthRequiredPaths() {
	}

	public static boolean requiresPageAuthentication(String uri) {
		if (uri == null || uri.isBlank()) {
			return false;
		}
		String path = DoPathHelper.stripDoSuffix(uri.trim());
		if (isAlwaysPublicPath(path)) {
			return false;
		}
		if (path.startsWith("/admin/")) {
			return true;
		}
		if ("/users".equals(path) || path.startsWith("/users/")) {
			return true;
		}
		if ("/dashboard".equals(path) || path.startsWith("/dashboard/")) {
			return true;
		}
		if (path.startsWith("/app/")) {
			return !isPublicStorefrontPath(path);
		}
		return false;
	}

	public static boolean isPublicApi(String uri) {
		if (uri == null || uri.isBlank()) {
			return false;
		}
		String path = uri.split("\\?")[0];
		if (path.startsWith("/api/url/")) {
			return true;
		}
		if (path.startsWith("/api/ecommerce/products/store-catalog")) {
			return true;
		}
		if (path.startsWith("/api/kakao/local")) {
			return true;
		}
		return path.matches("/api/ecommerce/products/\\d+");
	}

	public static boolean isSafeReturnUrl(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		String u = url.trim();
		return u.startsWith("/") && !u.startsWith("//") && !u.contains("://");
	}

	private static boolean isAlwaysPublicPath(String path) {
		return path.startsWith("/auth/")
				|| path.startsWith("/pages/authentication/")
				|| path.startsWith("/pages/errors/")
				|| path.startsWith("/pages/miscellaneous/")
				|| path.startsWith("/pages/landing")
				|| "/pages/starter".equals(path)
				|| path.startsWith("/modules/")
				|| path.startsWith("/documentation/")
				|| path.startsWith("/demo/")
				|| "/".equals(path)
				|| "/shop-home".equals(path);
	}

	private static boolean isPublicStorefrontPath(String path) {
		return "/shop-home".equals(path)
				|| path.startsWith("/app/e-commerce/product/product-grid")
				|| path.startsWith("/app/e-commerce/product/product-details")
				|| path.startsWith("/app/e-commerce/cart")
				|| path.startsWith("/app/e-commerce/checkout");
	}
}
