package com.example.springbootapp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import com.example.springbootapp.service.KakaoLocalApiDisabledException;
import com.example.springbootapp.service.KakaoLocalService;
/**
 * 카카오 로컬 API 프록시 REST API.
 * <p>기본 경로: {@code /api/kakao/local} — REST API 키는 서버에서만 사용합니다.</p>
 */
@RestController
@RequestMapping("/api/kakao/local")
public class KakaoLocalApiController {
	private final KakaoLocalService kakaoLocalService;
	public KakaoLocalApiController(KakaoLocalService kakaoLocalService) {
		this.kakaoLocalService = kakaoLocalService;
	}
	/**
	 * 카카오 로컬 API를 통해 주소를 검색합니다.
	 *
	 * @param query in: 검색 키워드
	 * @param page  in: 페이지 번호 (기본 1)
	 * @param size  in: 페이지당 결과 수 (기본 10)
	 * @return out: {@code ResponseEntity<Map>} — {@code items}, {@code configured}, {@code mock} 또는 503/403/400
	 */
	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status(
			@RequestParam(value = "origin", required = false) String origin,
			HttpServletRequest request) {
		Map<String, Object> body = new HashMap<>();
		body.put("configured", kakaoLocalService.isConfigured());
		body.put("mock", kakaoLocalService.usesMock());
		body.put("mapConfigured", kakaoLocalService.isMapKeyConfigured());
		if (kakaoLocalService.isMapKeyConfigured()) {
			body.put("mapAppKey", kakaoLocalService.getMapAppKey());
		}
		String accessOrigin = StringUtils.hasText(origin) ? origin.trim() : resolveAccessOrigin(request);
		if (StringUtils.hasText(accessOrigin)) {
			body.put("accessOrigin", accessOrigin);
		}
		body.put(
				"mapSdkDomainGuide",
				"developers.kakao.com → 내 애플리케이션 → "
						+ "① 제품 설정 → 지도(OPEN_MAP_AND_LOCAL) ON "
						+ "② 앱 설정 → 플랫폼 키 → Web → 사이트 도메인 등록 "
						+ "③ JavaScript 키 → JavaScript SDK 도메인 등록 (Web과 동일 주소)");
		body.put("mapSdkProxyUrl", "/api/kakao/local/maps-sdk.js");
		body.put("mapSdkDomainsSuggested", List.of(
				"http://localhost:8081",
				"http://127.0.0.1:8081",
				StringUtils.hasText(accessOrigin) ? accessOrigin : "http://localhost:8081"));
		if (kakaoLocalService.isConfigured()) {
			body.put("message", "카카오 REST API 키가 설정되어 있습니다.");
		} else {
			body.put(
					"message",
					"카카오 REST API 키가 없습니다. src/main/resources/application-local.properties 의 kakao.client-id 를 설정한 뒤 서버를 재시작하세요.");
		}
		if (!kakaoLocalService.isMapKeyConfigured()) {
			body.put(
					"mapMessage",
					"카카오맵 JavaScript 키가 없습니다. application-local.properties 의 kakao.javascript-key 를 설정한 뒤 서버를 재시작하세요.");
		}
		return ResponseEntity.ok(body);
	}

	private static String resolveAccessOrigin(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String scheme = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		if (!StringUtils.hasText(host)) {
			return "";
		}
		boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
				|| ("https".equalsIgnoreCase(scheme) && port == 443);
		return defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
	}

	@GetMapping("/search/address")
	public ResponseEntity<Map<String, Object>> searchAddress(
			@RequestParam("query") String query,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Map<String, Object> body = new HashMap<>();
		try {
			if (!kakaoLocalService.isConfigured() && !kakaoLocalService.usesMock()) {
				body.put("configured", false);
				body.put("message", "카카오 REST API 키가 설정되지 않았습니다.");
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
			}
			var result = kakaoLocalService.searchAddress(query, page, size);
			body.put("configured", kakaoLocalService.isConfigured());
			body.put("mock", result.mock());
			body.put("items", result.items());
			if (StringUtils.hasText(result.warning())) {
				body.put("warning", result.warning());
			}
			return ResponseEntity.ok(body);
		} catch (KakaoLocalApiDisabledException ex) {
			body.put("configured", kakaoLocalService.isConfigured());
			body.put("message", ex.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			body.put("configured", kakaoLocalService.isConfigured());
			body.put("message", ex.getMessage());
			return ResponseEntity.badRequest().body(body);
		}
	}
}
