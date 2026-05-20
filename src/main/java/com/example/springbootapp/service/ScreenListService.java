package com.example.springbootapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.config.ScreenSidebarLoader;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.mapper.ScreenListMapper;

@Service
public class ScreenListService {

	private static final String DEFAULT_ACTOR = "SYSTEM";

	private final ScreenListMapper screenListMapper;

	public ScreenListService(ScreenListMapper screenListMapper) {
		this.screenListMapper = screenListMapper;
	}

	@Transactional(readOnly = true)
	public List<ScreenList> findAllActive() {
		return screenListMapper.findAllActive();
	}

	@Transactional(readOnly = true)
	public List<ScreenList> searchForAdmin(String screenId, String screenNm, String uriPath, String useYn) {
		String nmFilter = trimToNull(screenNm);
		List<ScreenList> rows = screenListMapper.findForAdmin(
				trimToNull(screenId),
				null,
				trimToNull(uriPath),
				trimToNull(useYn));
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

	@Transactional(readOnly = true)
	public ScreenList findByScreenId(String screenId) {
		if (screenId == null || screenId.isBlank()) {
			return null;
		}
		return screenListMapper.findByScreenId(screenId.trim());
	}

	@Transactional(readOnly = true)
	public ScreenList findByUriPath(String uriPath) {
		String normalized = normalizeUriPath(uriPath);
		return screenListMapper.findByUriPath(normalized);
	}

	@Transactional(readOnly = true)
	public ScreenList resolveForRequest(String requestUri) {
		return findByUriPath(requestUri);
	}

	/** 사용여부 Y인 화면 URL 목록 (사이드바 메뉴 표시용) */
	@Transactional(readOnly = true)
	public List<String> findActiveUriPaths() {
		return findAllActive().stream()
				.map(ScreenList::getUriPath)
				.collect(Collectors.toList());
	}

	/** screen_list에 등록된 화면만 Y일 때 접근·표시 허용 */
	@Transactional(readOnly = true)
	public boolean isAccessible(String requestUri) {
		String uri = normalizeUriPath(requestUri);
		ScreenList screen = screenListMapper.findByUriPath(uri);
		if (screen == null) {
			return true;
		}
		return "Y".equalsIgnoreCase(screen.getUseYn());
	}

	public String normalizeUriPath(String uri) {
		if (uri == null || uri.isBlank()) {
			return "/";
		}
		String path = uri.split("\\?")[0].trim();
		if (path.endsWith(".html")) {
			path = path.substring(0, path.length() - 5);
		}
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path.isEmpty() ? "/" : path;
	}

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

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
