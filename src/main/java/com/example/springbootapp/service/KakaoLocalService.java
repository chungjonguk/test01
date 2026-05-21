package com.example.springbootapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.springbootapp.dto.KakaoAddressItemDto;

/**
 * 카카오 로컬 API — 주소 검색 (주소로 좌표 변환).
 *
 * @see <a href="https://developers.kakao.com/docs/ko/local/dev-guide">Kakao Local dev-guide</a>
 */
@Service
public class KakaoLocalService {

	private static final String PLACEHOLDER_KEY = "YOUR_KAKAO_REST_API_KEY";

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${kakao.rest-api-key:${kakao.client-id:}}")
	private String restApiKey;

	@Value("${kakao.local.address-search-url:https://dapi.kakao.com/v2/local/search/address.json}")
	private String addressSearchUrl;

	@Value("${kakao.local.keyword-search-url:https://dapi.kakao.com/v2/local/search/keyword.json}")
	private String keywordSearchUrl;

	@Value("${kakao.local.mock-enabled:true}")
	private boolean mockEnabled;

	private static final List<KakaoAddressItemDto> MOCK_ADDRESSES = List.of(
			new KakaoAddressItemDto(
					"서울특별시 강남구 테헤란로 152",
					"ROAD_ADDR",
					"06236",
					"서울특별시 강남구 테헤란로 152",
					"강남구",
					"서울특별시",
					"역삼동",
					"강남파이낸스센터",
					127.0365,
					37.5001),
			new KakaoAddressItemDto(
					"서울특별시 종로구 세종대로 209",
					"ROAD_ADDR",
					"03171",
					"서울특별시 종로구 세종대로 209",
					"종로구",
					"서울특별시",
					"세종로",
					"정부서울청사",
					126.9780,
					37.5665),
			new KakaoAddressItemDto(
					"서울특별시 마포구 월드컵북로 400",
					"ROAD_ADDR",
					"03925",
					"서울특별시 마포구 월드컵북로 400",
					"마포구",
					"서울특별시",
					"상암동",
					"DMC타워",
					126.8895,
					37.5791),
			new KakaoAddressItemDto(
					"부산광역시 해운대구 해운대해변로 264",
					"ROAD_ADDR",
					"48099",
					"부산광역시 해운대구 해운대해변로 264",
					"해운대구",
					"부산광역시",
					"중동",
					"해운대해수욕장",
					129.1658,
					35.1587));

	public boolean isConfigured() {
		return StringUtils.hasText(restApiKey) && !PLACEHOLDER_KEY.equals(restApiKey.trim());
	}

	public boolean usesMock() {
		return mockEnabled && !isConfigured();
	}

	public List<KakaoAddressItemDto> searchAddress(String query, int page, int size) {
		if (!StringUtils.hasText(query) || query.trim().length() < 2) {
			return Collections.emptyList();
		}
		if (usesMock()) {
			return mockSearch(query.trim(), size);
		}
		if (!isConfigured()) {
			throw new IllegalStateException(
					"카카오 REST API 키가 설정되지 않았습니다. application.properties 의 kakao.rest-api-key (또는 kakao.client-id)를 등록해 주세요.");
		}

		int safePage = Math.max(1, Math.min(page, 45));
		int safeSize = Math.max(1, Math.min(size, 30));

		List<KakaoAddressItemDto> items = fetchAddressDocuments(query.trim(), safePage, safeSize);
		if (items.isEmpty()) {
			items = fetchKeywordDocuments(query.trim(), safePage, Math.min(safeSize, 15));
		}
		return items;
	}

	private List<KakaoAddressItemDto> fetchAddressDocuments(String query, int page, int size) {
		String url = UriComponentsBuilder.fromHttpUrl(addressSearchUrl)
				.queryParam("query", query)
				.queryParam("page", page)
				.queryParam("size", size)
				.build()
				.encode()
				.toUriString();
		Map<String, Object> body = callKakaoApi(url);
		return mapAddressDocuments(body);
	}

	private List<KakaoAddressItemDto> fetchKeywordDocuments(String query, int page, int size) {
		String url = UriComponentsBuilder.fromHttpUrl(keywordSearchUrl)
				.queryParam("query", query)
				.queryParam("page", page)
				.queryParam("size", size)
				.build()
				.encode()
				.toUriString();
		Map<String, Object> body = callKakaoApi(url);
		return mapKeywordDocuments(body);
	}

	private Map<String, Object> callKakaoApi(String url) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + restApiKey.trim());

		ResponseEntity<Map<String, Object>> response;
		try {
			response = restTemplate.exchange(
					url,
					HttpMethod.GET,
					new HttpEntity<>(headers),
					new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
					});
		} catch (RestClientException ex) {
			throw new IllegalStateException("카카오 로컬 API 호출에 실패했습니다: " + ex.getMessage(), ex);
		}

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("카카오 로컬 API 응답이 올바르지 않습니다.");
		}
		return response.getBody();
	}

	private List<KakaoAddressItemDto> mockSearch(String query, int size) {
		int safeSize = Math.max(1, Math.min(size, 10));
		List<KakaoAddressItemDto> matched = MOCK_ADDRESSES.stream()
				.filter(item -> matchesMockQuery(item, query))
				.limit(safeSize)
				.toList();
		if (!matched.isEmpty()) {
			return matched;
		}
		return List.of(new KakaoAddressItemDto(
				"서울특별시 " + query,
				"MOCK",
				"",
				"서울특별시 " + query,
				"중구",
				"서울특별시",
				"",
				"",
				null,
				null));
	}

	private boolean matchesMockQuery(KakaoAddressItemDto item, String query) {
		String haystack = String.join(
				" ",
				item.displayAddress(),
				item.addressLine1(),
				item.city(),
				item.state(),
				item.region3(),
				item.buildingName());
		return haystack.contains(query);
	}

	@SuppressWarnings("unchecked")
	private List<KakaoAddressItemDto> mapAddressDocuments(Map<String, Object> body) {
		Object documentsObj = body.get("documents");
		if (!(documentsObj instanceof List<?> documents)) {
			return Collections.emptyList();
		}

		List<KakaoAddressItemDto> items = new ArrayList<>();
		for (Object docObj : documents) {
			if (docObj instanceof Map<?, ?> doc) {
				KakaoAddressItemDto item = mapDocument((Map<String, Object>) doc);
				if (item != null) {
					items.add(item);
				}
			}
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	private List<KakaoAddressItemDto> mapKeywordDocuments(Map<String, Object> body) {
		Object documentsObj = body.get("documents");
		if (!(documentsObj instanceof List<?> documents)) {
			return Collections.emptyList();
		}

		List<KakaoAddressItemDto> items = new ArrayList<>();
		for (Object docObj : documents) {
			if (docObj instanceof Map<?, ?> doc) {
				KakaoAddressItemDto item = mapKeywordDocument((Map<String, Object>) doc);
				if (item != null) {
					items.add(item);
				}
			}
		}
		return items;
	}

	private KakaoAddressItemDto mapKeywordDocument(Map<String, Object> doc) {
		String road = stringVal(doc.get("road_address_name"));
		String jibun = stringVal(doc.get("address_name"));
		String placeName = stringVal(doc.get("place_name"));

		String displayAddress = StringUtils.hasText(road) ? road : jibun;
		if (!StringUtils.hasText(displayAddress)) {
			return null;
		}

		String addressType = StringUtils.hasText(road) ? "ROAD_ADDR" : "REGION_ADDR";
		Double x = parseDouble(doc.get("x"));
		Double y = parseDouble(doc.get("y"));

		return new KakaoAddressItemDto(
				displayAddress,
				addressType,
				"",
				displayAddress,
				"",
				"",
				"",
				placeName,
				x,
				y);
	}

	private KakaoAddressItemDto mapDocument(Map<String, Object> doc) {
		Map<String, Object> road = asMap(doc.get("road_address"));
		Map<String, Object> jibun = asMap(doc.get("address"));

		String displayAddress = stringVal(doc.get("address_name"));
		String addressType = stringVal(doc.get("address_type"));

		String postalCode = "";
		String addressLine1 = displayAddress;
		String city = "";
		String state = "";
		String region3 = "";
		String buildingName = "";

		if (road != null && StringUtils.hasText(stringVal(road.get("address_name")))) {
			addressType = "ROAD_ADDR";
			addressLine1 = stringVal(road.get("address_name"));
			displayAddress = addressLine1;
			postalCode = stringVal(road.get("zone_no"));
			state = stringVal(road.get("region_1depth_name"));
			city = stringVal(road.get("region_2depth_name"));
			region3 = stringVal(road.get("region_3depth_name"));
			buildingName = stringVal(road.get("building_name"));
		} else if (jibun != null && StringUtils.hasText(stringVal(jibun.get("address_name")))) {
			addressType = "REGION_ADDR";
			addressLine1 = stringVal(jibun.get("address_name"));
			displayAddress = addressLine1;
			state = stringVal(jibun.get("region_1depth_name"));
			city = stringVal(jibun.get("region_2depth_name"));
			region3 = stringVal(jibun.get("region_3depth_name"));
		}

		if (!StringUtils.hasText(displayAddress)) {
			return null;
		}

		Double x = parseDouble(doc.get("x"));
		Double y = parseDouble(doc.get("y"));

		return new KakaoAddressItemDto(
				displayAddress,
				addressType,
				postalCode,
				addressLine1,
				city,
				state,
				region3,
				buildingName,
				x,
				y);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return null;
	}

	private String stringVal(Object value) {
		return value == null ? "" : Objects.toString(value, "").trim();
	}

	private Double parseDouble(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return Double.parseDouble(Objects.toString(value, "").trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
