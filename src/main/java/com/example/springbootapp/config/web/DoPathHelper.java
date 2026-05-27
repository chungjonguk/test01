package com.example.springbootapp.config.web;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import com.example.springbootapp.config.CustomErrorController;
import com.example.springbootapp.controller.KakaoMapSdkController;
import com.example.springbootapp.controller.LegacyPathRedirectController;

/**
 * 컨트롤러·화면·API 경로에 {@code .do} 접미사를 붙이기 위한 공통 규칙.
 * <ul>
 *   <li>매핑 변환: {@link #addDoSuffix(RequestMappingInfo)}</li>
 *   <li>화면 URI 정규화: {@link #normalizeForScreenLookup(String)}</li>
 *   <li>제외: {@code /auth/**}, 정적 리소스, 이미 확장자 있는 경로</li>
 * </ul>
 */
public final class DoPathHelper {

	private static final Set<String> EXCLUDED_PREFIXES = Set.of(
			"/auth/",
			"/assets/",
			"/vendors/",
			"/error",
			"/api/");

	private DoPathHelper() {
	}

	/** 레거시 리다이렉트·에러·카카오 SDK 등 .do 미적용 컨트롤러 */
	public static boolean isExcludedHandler(Class<?> handlerType) {
		if (handlerType == null) {
			return true;
		}
		return LegacyPathRedirectController.class.isAssignableFrom(handlerType)
				|| CustomErrorController.class.isAssignableFrom(handlerType)
				|| KakaoMapSdkController.class.isAssignableFrom(handlerType);
	}

	/** 암호화·공개 URL 변환 제외 (API·정적). 논리 경로 {@code *.do} 는 암호화 대상 */
	public static boolean shouldSkipEncryption(String path) {
		if (path == null || path.isBlank()) {
			return true;
		}
		String p = stripQuery(path);
		for (String prefix : EXCLUDED_PREFIXES) {
			if (p.startsWith(prefix)) {
				return true;
			}
		}
		if (p.endsWith(".do") && !p.startsWith("/e/")) {
			return false;
		}
		return hasNonDoFileExtension(p);
	}

	/** .do 접미사·리다이렉트 대상에서 제외할 경로 여부 */
	public static boolean shouldSkipSuffix(String path) {
		if (path == null || path.isBlank()) {
			return true;
		}
		String p = stripQuery(path);
		for (String prefix : EXCLUDED_PREFIXES) {
			if (p.startsWith(prefix)) {
				return true;
			}
		}
		if (p.endsWith(".do")) {
			return true;
		}
		return hasFileExtension(p);
	}

	/** GET 리다이렉트 필터 제외 */
	public static boolean shouldSkipRedirect(String path) {
		return shouldSkipSuffix(path);
	}

	/** 경로를 .do 형식으로 변환 ({@code /} → {@code /index.do}) */
	public static String toDoPath(String path) {
		if (path == null || path.isBlank()) {
			return "/index.do";
		}
		String p = stripQuery(path.trim());
		if (shouldSkipSuffix(p)) {
			return p.isEmpty() ? "/" : p;
		}
		if (p.endsWith(".do")) {
			return p;
		}
		if ("/".equals(p)) {
			return "/index.do";
		}
		if (p.length() > 1 && p.endsWith("/")) {
			p = p.substring(0, p.length() - 1);
		}
		return p + ".do";
	}

	/** .do 접미사 제거 (screen_table_map·스크립트 플래그 비교용) */
	public static String stripDoSuffix(String path) {
		if (path == null || path.isBlank()) {
			return path;
		}
		String p = stripQuery(path.trim());
		if (p.endsWith(".do") && p.length() > 3) {
			p = p.substring(0, p.length() - 3);
		}
		if ("/index".equals(p)) {
			return "/";
		}
		return p.isEmpty() ? "/" : p;
	}

	public static RequestMappingInfo addDoSuffix(RequestMappingInfo info) {
		PathPatternsRequestCondition paths = info.getPathPatternsCondition();
		if (paths == null || paths.getPatterns().isEmpty()) {
			return info;
		}
		Set<String> mapped = new LinkedHashSet<>();
		for (var pattern : paths.getPatterns()) {
			String raw = pattern.getPatternString();
			if (shouldSkipSuffix(raw) || raw.contains("{") || raw.contains("*")) {
				mapped.add(raw);
			} else {
				mapped.add(toDoPath(raw));
			}
		}
		if (mapped.isEmpty()) {
			return info;
		}
		return info.mutate().paths(mapped.toArray(String[]::new)).build();
	}

	public static String normalizeForScreenLookup(String uri) {
		if (uri == null || uri.isBlank()) {
			return "/index.do";
		}
		String path = stripQuery(uri.trim());
		if (path.endsWith(".html")) {
			path = path.substring(0, path.length() - 5);
		}
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if ("/".equals(path)) {
			return "/index.do";
		}
		if (shouldSkipSuffix(path)) {
			return path.isEmpty() ? "/" : path;
		}
		return toDoPath(path);
	}

	private static boolean hasFileExtension(String path) {
		int slash = path.lastIndexOf('/');
		String last = slash >= 0 ? path.substring(slash + 1) : path;
		int dot = last.indexOf('.');
		return dot > 0 && dot < last.length() - 1;
	}

	private static boolean hasNonDoFileExtension(String path) {
		if (path == null || path.endsWith(".do")) {
			return false;
		}
		return hasFileExtension(path);
	}

	/**
	 * 쿼리·세션 path parameter 제거.
	 * <p>잘못된 {@code /path;jsessionid=xxx.do} 형태는 {@code /path.do} 로 복구합니다.</p>
	 */
	public static String sanitizeRequestPath(String uri) {
		if (uri == null || uri.isBlank()) {
			return uri;
		}
		String path = uri.trim();
		int q = path.indexOf('?');
		String query = q >= 0 ? path.substring(q) : "";
		path = q >= 0 ? path.substring(0, q) : path;
		int semi = path.indexOf(';');
		if (semi >= 0) {
			String before = path.substring(0, semi);
			String after = path.substring(semi + 1);
			int doIdx = after.indexOf(".do");
			if (doIdx >= 0 && !before.endsWith(".do")) {
				path = before + ".do";
			} else {
				path = before;
			}
		}
		return path + query;
	}

	private static String stripQuery(String uri) {
		String sanitized = sanitizeRequestPath(uri);
		int q = sanitized.indexOf('?');
		return q >= 0 ? sanitized.substring(0, q) : sanitized;
	}

	static boolean appliesToController(Class<?> handlerType) {
		if (handlerType == null || isExcludedHandler(handlerType)) {
			return false;
		}
		// REST API는 경로 암호화·.do 접미사 미적용 (fetch가 /api/... 그대로 호출)
		if (handlerType.isAnnotationPresent(RestController.class)) {
			return false;
		}
		return handlerType.isAnnotationPresent(org.springframework.stereotype.Controller.class);
	}
}
