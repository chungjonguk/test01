package com.example.springbootapp.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.config.ScreenSidebarLoader;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.mapper.ScreenListMapper;
import org.springframework.beans.factory.ObjectProvider;
/**
 * 화면(screen_list) 목록 조회·접근 제어·URI 정규화·저장을 처리하는 서비스.
 */
@Service
public class ScreenListService {
	private static final String DEFAULT_ACTOR = "SYSTEM";
	private final ScreenListMapper screenListMapper;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public ScreenListService(
			ScreenListMapper screenListMapper,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.screenListMapper = screenListMapper;
		this.publicPathCrypto = publicPathCrypto;
	}
	/**
	 * 사용 중인 활성 화면 목록을 조회한다.
	 *
	 * @return 활성 화면 엔티티 목록
	 */
	@Transactional(readOnly = true)
	public List<ScreenList> findAllActive() {
		return screenListMapper.findAllActive();
	}
	/**
	 * 관리자 화면에서 조건에 맞는 화면 목록을 검색한다.
	 *
	 * @param screenId 화면 ID (부분 일치, null 허용)
	 * @param screenNm 화면명 (표시명 기준 부분 일치, null 허용)
	 * @param uriPath  URI 경로 (부분 일치, null 허용)
	 * @param useYn    사용 여부 Y/N (null 허용)
	 * @return 화면 엔티티 목록
	 */
	@Transactional(readOnly = true)
	public List<ScreenList> searchForAdmin(String screenId, String screenNm, String uriPath, String useYn) {
		String nmFilter = trimToNull(screenNm);
		List<ScreenList> rows = dedupeByCanonicalUri(screenListMapper.findForAdmin(
				trimToNull(screenId),
				null,
				trimToNull(uriPath),
				trimToNull(useYn)));
		if (nmFilter == null) {
			return rows;
		}
		String needle = nmFilter.toLowerCase();
		return rows.stream()
				.filter(screen -> ScreenSidebarLoader
						.resolveDisplayName(screen.getScreenNm(), screen.getUriPath())
						.toLowerCase()
						.contains(needle))
				.collect(Collectors.toList());
	}
	/**
	 * 화면 ID로 단건을 조회한다.
	 *
	 * @param screenId 화면 ID
	 * @return 화면 엔티티, 없으면 null
	 */
	@Transactional(readOnly = true)
	public ScreenList findByScreenId(String screenId) {
		if (screenId == null || screenId.isBlank()) {
			return null;
		}
		return screenListMapper.findByScreenId(screenId.trim());
	}
	/**
	 * URI 경로로 화면 정보를 조회한다.
	 *
	 * @param uriPath URI 경로 (정규화 후 조회)
	 * @return 화면 엔티티, 없으면 null
	 */
	@Transactional(readOnly = true)
	public ScreenList findByUriPath(String uriPath) {
		String normalized = normalizeUriPath(uriPath);
		ScreenList screen = screenListMapper.findByUriPath(normalized);
		if (screen != null) {
			return screen;
		}
		if ("/index.do".equals(normalized)) {
			screen = screenListMapper.findByUriPath("/");
			if (screen != null) {
				return screen;
			}
		}
		if ("/shop-dashboard.do".equals(normalized)) {
			screen = screenListMapper.findByUriPath("/shop-dashboard");
			if (screen != null) {
				return screen;
			}
		}
		String stripped = DoPathHelper.stripDoSuffix(normalized);
		if (!stripped.equals(normalized)) {
			return screenListMapper.findByUriPath(stripped);
		}
		return null;
	}
	/**
	 * HTTP 요청 URI에 해당하는 화면 정보를 조회한다.
	 *
	 * @param requestUri 요청 URI
	 * @return 화면 엔티티, 없으면 null
	 */
	@Transactional(readOnly = true)
	public ScreenList resolveForRequest(String requestUri) {
		return findByUriPath(requestUri);
	}
	/**
	 * 사용 여부가 Y인 화면 URI 경로 목록을 조회한다 (사이드바 메뉴 표시용).
	 *
	 * @return 활성 화면 URI 경로 목록
	 */
	@Transactional(readOnly = true)
	public List<String> findActiveUriPaths() {
		return findAllActive().stream()
				.map(ScreenList::getUriPath)
				.collect(Collectors.toList());
	}
	/**
	 * 요청 URI에 대한 화면 접근이 허용되는지 확인한다. 미등록 URI는 허용한다.
	 *
	 * @param requestUri 요청 URI
	 * @return 접근 허용 여부
	 */
	@Transactional(readOnly = true)
	public boolean isAccessible(String requestUri) {
		String uri = normalizeUriPath(requestUri);
		ScreenList screen = screenListMapper.findByUriPath(uri);
		if (screen == null) {
			return true;
		}
		return "Y".equalsIgnoreCase(screen.getUseYn());
	}
	/**
	 * URI 경로를 비교·조회용 표준 형식으로 정규화한다.
	 *
	 * @param uri 원본 URI
	 * @return 정규화된 URI 경로
	 */
	public String normalizeUriPath(String uri) {
		if (uri == null || uri.isBlank()) {
			return "/index.do";
		}
		String path = uri.split("\\?")[0].trim();
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			path = crypto.toLogicalPath(path);
		}
		if (path.endsWith(".html")) {
			path = path.substring(0, path.length() - 5);
		}
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return DoPathHelper.normalizeForScreenLookup(path.isEmpty() ? "/" : path);
	}
	/**
	 * 화면 정보를 신규 등록하거나 기존 화면을 수정한다. 동일 URI가 있으면 PK를 유지한다.
	 *
	 * @param screen  화면 엔티티
	 * @param actorId 등록·수정자 ID
	 */
	@Transactional
	public void save(ScreenList screen, String actorId) {
		if (screen == null || screen.getScreenId() == null || screen.getScreenId().isBlank()) {
			throw new IllegalArgumentException("screenId는 필수입니다.");
		}
		String actor = actorId != null && !actorId.isBlank() ? actorId : DEFAULT_ACTOR;
		LocalDateTime now = LocalDateTime.now();
		screen.setUriPath(normalizeUriPath(screen.getUriPath()));
		if (screen.getUseYn() == null || screen.getUseYn().isBlank()) {
			screen.setUseYn("Y");
		}
		// URI가 같으면 기존 PK 유지 (screen_id 명명 규칙이 바뀌어도 중복 INSERT 방지)
		ScreenList existing = screenListMapper.findByUriPath(screen.getUriPath());
		if (existing == null) {
			existing = screenListMapper.findByScreenId(screen.getScreenId());
		}
		if (existing != null && shouldReplaceShopRow(existing.getScreenId(), screen.getScreenId())) {
			screenListMapper.deleteByScreenId(existing.getScreenId());
			existing = null;
		}
		if (existing == null) {
			screen.setRegId(actor);
			screen.setRegDt(now);
			screen.setUpdateId(actor);
			screen.setUpdateDt(now);
			screenListMapper.insert(screen);
		} else {
			screen.setScreenId(existing.getScreenId());
			screen.setRegId(existing.getRegId());
			screen.setRegDt(existing.getRegDt());
			screen.setUpdateId(actor);
			screen.setUpdateDt(now);
			screenListMapper.update(screen);
		}
	}
	/**
	 * 동일 논리 URI(.do 정규화 기준) 중복 행을 하나로 합친다.
	 */
	static List<ScreenList> dedupeByCanonicalUri(List<ScreenList> rows) {
		if (rows == null || rows.isEmpty()) {
			return List.of();
		}
		Map<String, ScreenList> byUri = new LinkedHashMap<>();
		for (ScreenList screen : rows) {
			String key = DoPathHelper.normalizeForScreenLookup(screen.getUriPath());
			ScreenList existing = byUri.get(key);
			if (existing == null || preferScreenRow(screen, existing)) {
				byUri.put(key, screen);
			}
		}
		return new ArrayList<>(byUri.values());
	}

	private static boolean preferScreenRow(ScreenList candidate, ScreenList incumbent) {
		return screenPreferenceScore(candidate) > screenPreferenceScore(incumbent);
	}

	private static int screenPreferenceScore(ScreenList screen) {
		int score = 0;
		String uri = screen.getUriPath();
		String id = screen.getScreenId();
		if (uri != null && uri.endsWith(".do")) {
			score += 100;
		}
		if (id != null && !id.endsWith(".DO")) {
			score += 50;
		}
		if (id != null && !id.startsWith("SHOP_")) {
			score += 30;
		}
		if (id != null && (id.startsWith("ADMIN_") || id.startsWith("APP_") || id.startsWith("ECM_")
				|| id.startsWith("DASHBOARD_") || "HOME".equals(id))) {
			score += 20;
		}
		return score;
	}

	private static boolean shouldReplaceShopRow(String existingId, String incomingId) {
		if (existingId == null || incomingId == null) {
			return false;
		}
		return existingId.startsWith("SHOP_") && !incomingId.startsWith("SHOP_");
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
