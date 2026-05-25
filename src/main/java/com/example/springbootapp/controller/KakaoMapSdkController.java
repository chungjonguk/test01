package com.example.springbootapp.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.example.springbootapp.service.KakaoLocalService;

/**
 * 카카오맵 JS SDK — 동일 출처(localhost:8081)로 제공해 브라우저 스크립트 차단을 줄입니다.
 */
@RestController
@RequestMapping("/api/kakao/local")
public class KakaoMapSdkController {
	private final KakaoLocalService kakaoLocalService;
	private final RestTemplate restTemplate = new RestTemplate();

	public KakaoMapSdkController(KakaoLocalService kakaoLocalService) {
		this.kakaoLocalService = kakaoLocalService;
	}

	@GetMapping(value = "/maps-sdk.js", produces = "application/javascript;charset=UTF-8")
	public ResponseEntity<byte[]> mapsSdk() {
		if (!kakaoLocalService.isMapKeyConfigured()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.contentType(MediaType.TEXT_PLAIN)
					.body("/* Kakao map JavaScript key not configured */".getBytes());
		}
		String appKey = kakaoLocalService.getMapAppKey();
		String url = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=" + appKey + "&autoload=false";
		byte[] body = restTemplate.getForObject(url, byte[].class);
		if (body == null || body.length == 0) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/javascript;charset=UTF-8"));
		headers.setCacheControl(CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS).cachePublic());
		return new ResponseEntity<>(body, headers, HttpStatus.OK);
	}
}
