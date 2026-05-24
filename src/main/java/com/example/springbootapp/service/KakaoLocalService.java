package com.example.springbootapp.service;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
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
	@Value("${kakao.local.coord2address-url:https://dapi.kakao.com/v2/local/geo/coord2address.json}")
	private String coord2AddressUrl;
	private static final Map<String, String> REGION1_FULL_NAMES = Map.ofEntries(
			Map.entry("서울", "서울특별시"),
			Map.entry("부산", "부산광역시"),
			Map.entry("대구", "대구광역시"),
			Map.entry("인천", "인천광역시"),
			Map.entry("광주", "광주광역시"),
			Map.entry("대전", "대전광역시"),
			Map.entry("울산", "울산광역시"),
			Map.entry("세종", "세종특별자치시"));
	private static final Pattern ADDRESS_PREFIX_PATTERN = Pattern.compile(
			"^((?:서울|부산|대구|인천|광주|대전|울산|세종)(?:특별시|광역시|특별자치시)?|"
					+ "[가-힣]+도)\\s+([가-힣]+(?:시|군|구))\\s+(.+)$");
	@Value("${kakao.local.mock-enabled:true}")
	private boolean mockEnabled;
	/** 지도/로컬 API 미활성화(403) 시 데모 주소로 대체 */
	@Value("${kakao.local.fallback-mock-on-local-disabled:true}")
	private boolean fallbackMockOnLocalDisabled;
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
	/**
	 * 카카오 REST API 키가 유효하게 설정되어 있는지 확인한다.
	 *
	 * @return API 키가 설정되어 있으면 true
	 */
	public boolean isConfigured() {
		return StringUtils.hasText(restApiKey) && !PLACEHOLDER_KEY.equals(restApiKey.trim());
	}
	/**
	 * 데모(목) 주소 검색 모드를 사용하는지 확인한다.
	 *
	 * @return mockEnabled이고 API 키가 없으면 true
	 */
	public boolean usesMock() {
		return mockEnabled && !isConfigured();
	}
	/**
	 * 주소·키워드로 카카오 로컬 API를 호출하여 주소 검색 결과를 반환한다.
	 *
	 * @param query 검색어 (2자 이상)
	 * @param page  페이지 번호 (1~45)
	 * @param size  페이지당 건수 (1~30)
	 * @return 검색 결과 (실제 API 또는 목 데이터)
	 */
	public KakaoAddressSearchResult searchAddress(String query, int page, int size) {
		if (!StringUtils.hasText(query) || query.trim().length() < 2) {
			return KakaoAddressSearchResult.live(Collections.emptyList());
		}
		String trimmed = query.trim();
		if (usesMock()) {
			return KakaoAddressSearchResult.mockFallback(
					mockSearch(trimmed, size),
					"데모 주소입니다. REST API 키를 application-local.properties 에 설정하면 실제 검색을 사용할 수 있습니다.");
		}
		if (!isConfigured()) {
			throw new IllegalStateException(
					"카카오 REST API 키가 설정되지 않았습니다. application.properties 의 kakao.rest-api-key (또는 kakao.client-id)를 등록해 주세요.");
		}
		int safePage = Math.max(1, Math.min(page, 45));
		int safeSize = Math.max(1, Math.min(size, 30));
		try {
			List<KakaoAddressItemDto> items = mergeResults(
					fetchAddressDocuments(trimmed, safePage, safeSize),
					fetchKeywordDocuments(trimmed, safePage, Math.min(safeSize, 15)),
					safeSize);
			return KakaoAddressSearchResult.live(items);
		} catch (KakaoLocalApiDisabledException ex) {
			if (fallbackMockOnLocalDisabled) {
				return KakaoAddressSearchResult.mockFallback(
						mockSearch(trimmed, size),
						KakaoLocalApiDisabledException.USER_MESSAGE + " (아래는 임시 데모 결과입니다.)");
			}
			throw ex;
		}
	}
	private List<KakaoAddressItemDto> fetchAddressDocuments(String query, int page, int size) {
		Map<String, Object> body = callKakaoApi(buildSearchUri(addressSearchUrl, query, page, size));
		return mapAddressDocuments(body);
	}
	private List<KakaoAddressItemDto> fetchKeywordDocuments(String query, int page, int size) {
		Map<String, Object> body = callKakaoApi(buildSearchUri(keywordSearchUrl, query, page, size));
		return mapKeywordDocuments(body);
	}
	private URI buildSearchUri(String baseUrl, String query, int page, int size) {
		return UriComponentsBuilder.fromUriString(baseUrl)
				.queryParam("query", query)
				.queryParam("page", page)
				.queryParam("size", size)
				.encode(StandardCharsets.UTF_8)
				.build()
				.toUri();
	}
	private List<KakaoAddressItemDto> mergeResults(
			List<KakaoAddressItemDto> addressItems,
			List<KakaoAddressItemDto> keywordItems,
			int maxSize) {
		List<KakaoAddressItemDto> merged = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (KakaoAddressItemDto item : addressItems) {
			if (seen.add(item.displayAddress())) {
				merged.add(item);
			}
			if (merged.size() >= maxSize) {
				return merged;
			}
		}
		for (KakaoAddressItemDto item : keywordItems) {
			if (seen.add(item.displayAddress())) {
				merged.add(item);
			}
			if (merged.size() >= maxSize) {
				break;
			}
		}
		return merged;
	}
	private Map<String, Object> callKakaoApi(URI uri) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + restApiKey.trim());
		ResponseEntity<Map<String, Object>> response;
		try {
			response = restTemplate.exchange(
					uri,
					HttpMethod.GET,
					new HttpEntity<>(headers),
					new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
					});
		} catch (HttpStatusCodeException ex) {
			if (isLocalApiDisabledError(ex)) {
				throw new KakaoLocalApiDisabledException();
			}
			throw new IllegalStateException("카카오 로컬 API 호출에 실패했습니다: " + summarizeHttpError(ex), ex);
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
	private boolean isLocalApiDisabledError(HttpStatusCodeException ex) {
		if (ex.getStatusCode() != HttpStatus.FORBIDDEN) {
			return false;
		}
		String body = ex.getResponseBodyAsString();
		return body != null
				&& (body.contains("OPEN_MAP_AND_LOCAL") || body.contains("disabled") && body.contains("LOCAL"));
	}
	private String summarizeHttpError(HttpStatusCodeException ex) {
		if (isLocalApiDisabledError(ex)) {
			return KakaoLocalApiDisabledException.USER_MESSAGE;
		}
		return ex.getStatusCode() + " "
				+ (StringUtils.hasText(ex.getStatusText()) ? ex.getStatusText() : ex.getMessage());
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
					items.add(enrichFromCoordinates(item));
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
		return withFullDisplay(new KakaoAddressItemDto(
				displayAddress,
				addressType,
				postalCode,
				addressLine1,
				city,
				state,
				region3,
				buildingName,
				x,
				y));
	}
	private KakaoAddressItemDto enrichFromCoordinates(KakaoAddressItemDto item) {
		if (item.longitude() == null || item.latitude() == null || hasCompleteAddress(item)) {
			return withFullDisplay(item);
		}
		try {
			URI uri = UriComponentsBuilder.fromUriString(coord2AddressUrl)
					.queryParam("x", item.longitude())
					.queryParam("y", item.latitude())
					.encode(StandardCharsets.UTF_8)
					.build()
					.toUri();
			Map<String, Object> body = callKakaoApi(uri);
			Object documentsObj = body.get("documents");
			if (documentsObj instanceof List<?> documents && !documents.isEmpty()
					&& documents.get(0) instanceof Map<?, ?> firstDoc) {
				@SuppressWarnings("unchecked")
				KakaoAddressItemDto resolved = mapDocument((Map<String, Object>) firstDoc);
				if (resolved != null) {
					String buildingName = StringUtils.hasText(item.buildingName())
							? item.buildingName()
							: resolved.buildingName();
					return withFullDisplay(new KakaoAddressItemDto(
							resolved.addressLine1(),
							resolved.addressType(),
							resolved.postalCode(),
							resolved.addressLine1(),
							resolved.city(),
							resolved.state(),
							resolved.region3(),
							buildingName,
							item.longitude(),
							item.latitude()));
				}
			}
		} catch (RuntimeException ignored) {
			// 좌표 변환 실패 시 문자열 파싱으로 보완
		}
		return withFullDisplay(parseAddressFromLine(item));
	}
	private boolean hasCompleteAddress(KakaoAddressItemDto item) {
		return StringUtils.hasText(item.postalCode())
				&& StringUtils.hasText(item.state())
				&& StringUtils.hasText(item.city());
	}
	private KakaoAddressItemDto parseAddressFromLine(KakaoAddressItemDto item) {
		String line = item.addressLine1();
		if (!StringUtils.hasText(line)) {
			return item;
		}
		Matcher matcher = ADDRESS_PREFIX_PATTERN.matcher(line.trim());
		if (!matcher.matches()) {
			return item;
		}
		return new KakaoAddressItemDto(
				line,
				item.addressType(),
				item.postalCode(),
				matcher.group(3).trim(),
				matcher.group(2).trim(),
				normalizeRegion1(matcher.group(1).trim()),
				item.region3(),
				item.buildingName(),
				item.longitude(),
				item.latitude());
	}
	private KakaoAddressItemDto withFullDisplay(KakaoAddressItemDto item) {
		String state = normalizeRegion1(item.state());
		String city = stringVal(item.city());
		String region3 = stringVal(item.region3());
		String addressLine1 = stringVal(item.addressLine1());
		String postalCode = stringVal(item.postalCode());
		String buildingName = stringVal(item.buildingName());
		String displayAddress = buildFullDisplayAddress(postalCode, state, city, region3, addressLine1, buildingName);
		return new KakaoAddressItemDto(
				displayAddress,
				item.addressType(),
				postalCode,
				addressLine1,
				city,
				state,
				region3,
				buildingName,
				item.longitude(),
				item.latitude());
	}
	private String buildFullDisplayAddress(
			String postalCode,
			String state,
			String city,
			String region3,
			String addressLine1,
			String buildingName) {
		String line = stringVal(addressLine1);
		StringBuilder sb = new StringBuilder();
		if (StringUtils.hasText(postalCode)) {
			sb.append('(').append(postalCode).append(") ");
		}
		if (StringUtils.hasText(line) && containsAdminDivision(line, state, city, region3)) {
			sb.append(line);
		} else {
			if (StringUtils.hasText(state)) {
				sb.append(state).append(' ');
			}
			if (StringUtils.hasText(city)) {
				sb.append(city).append(' ');
			}
			if (StringUtils.hasText(region3)) {
				sb.append(region3).append(' ');
			}
			if (StringUtils.hasText(line)) {
				sb.append(line);
			}
		}
		if (StringUtils.hasText(buildingName) && !sb.toString().contains(buildingName)) {
			if (!sb.isEmpty()) {
				sb.append(' ');
			}
			sb.append(buildingName);
		}
		return sb.toString().trim();
	}
	private boolean containsAdminDivision(String line, String state, String city, String region3) {
		if (StringUtils.hasText(state)) {
			String shortState = state.replace("특별시", "").replace("광역시", "").replace("특별자치시", "");
			if (line.contains(state) || (StringUtils.hasText(shortState) && line.contains(shortState))) {
				return true;
			}
		}
		if (StringUtils.hasText(city) && line.contains(city)) {
			return true;
		}
		return StringUtils.hasText(region3) && line.contains(region3);
	}
	private String normalizeRegion1(String region1) {
		if (!StringUtils.hasText(region1)) {
			return "";
		}
		String trimmed = region1.trim();
		if (trimmed.endsWith("특별시") || trimmed.endsWith("광역시") || trimmed.endsWith("특별자치시") || trimmed.endsWith("도")) {
			return trimmed;
		}
		return REGION1_FULL_NAMES.getOrDefault(trimmed, trimmed);
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
