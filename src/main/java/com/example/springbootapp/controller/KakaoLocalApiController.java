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
