package com.example.springbootapp.controller;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.ScreenSidebarLoader;
import com.example.springbootapp.config.ScreenTableResolver;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.domain.ScreenTableMap;
import com.example.springbootapp.service.ScreenListService;
import com.example.springbootapp.service.ScreenTableMapService;
import jakarta.servlet.http.HttpSession;
/**
 * 관리자 메뉴(화면) REST API.
 * <p>기본 경로: {@code /api/admin/menus}</p>
 */
@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuApiController {
	private final ScreenListService screenListService;
	private final ScreenTableMapService screenTableMapService;
	private final SessionAuthService sessionAuthService;
	public AdminMenuApiController(
			ScreenListService screenListService,
			ScreenTableMapService screenTableMapService,
			SessionAuthService sessionAuthService) {
		this.screenListService = screenListService;
		this.screenTableMapService = screenTableMapService;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 화면(메뉴) 목록을 조건에 따라 검색합니다.
	 *
	 * @param screenId in: 화면 ID (선택)
	 * @param screenNm in: 화면명 (선택)
	 * @param uriPath  in: URI 경로 (선택)
	 * @param useYn    in: 사용 여부 Y/N (선택)
	 * @return out: {@code ResponseEntity<Map>} — {@code screens}, {@code count}
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String screenId,
			@RequestParam(required = false) String screenNm,
			@RequestParam(required = false) String uriPath,
			@RequestParam(required = false) String useYn) {
		Map<String, ScreenTableMap> tableByUri = screenTableMapService.findAllByUri();
		List<Map<String, Object>> screens = screenListService.searchForAdmin(screenId, screenNm, uriPath, useYn)
				.stream()
				.map(screen -> toDto(screen, resolveTableMap(tableByUri, screen.getUriPath())))
				.collect(Collectors.toList());
		Map<String, Object> body = new HashMap<>();
		body.put("screens", screens);
		body.put("count", screens.size());
		return ResponseEntity.ok(body);
	}

	/**
	 * 화면(메뉴) 정보를 등록·수정합니다.
	 *
	 * @param body    in: screenId, screenNm, uriPath, sortOrd, useYn 등
	 * @param session in: 수정자 식별용 HTTP 세션
	 * @return out: success, screenId, message
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, Object> body, HttpSession session) {
		try {
			ScreenList screen = toEntity(body);
			String actor = sessionAuthService.getLoginUserId(session);
			screenListService.save(screen, actor);
			Map<String, Object> res = new LinkedHashMap<>();
			res.put("success", true);
			res.put("screenId", screen.getScreenId());
			res.put("message", "메뉴 정보가 저장되었습니다.");
			return ResponseEntity.ok(res);
		} catch (IllegalArgumentException ex) {
			Map<String, Object> res = new LinkedHashMap<>();
			res.put("success", false);
			res.put("message", ex.getMessage());
			return ResponseEntity.badRequest().body(res);
		}
	}

	private static ScreenList toEntity(Map<String, Object> body) {
		if (body == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		ScreenList screen = new ScreenList();
		screen.setScreenId(asString(body.get("screenId")));
		screen.setScreenNm(asString(body.get("screenNm")));
		screen.setUriPath(asString(body.get("uriPath")));
		screen.setTemplatePath(asString(body.get("templatePath")));
		screen.setUseYn(asString(body.get("useYn")));
		screen.setMenuGroup(asString(body.get("menuGroup")));
		screen.setIconClass(asString(body.get("iconClass")));
		screen.setParentScreenId(asString(body.get("parentScreenId")));
		Object sort = body.get("sortOrd");
		if (sort instanceof Number number) {
			screen.setSortOrd(number.intValue());
		} else if (sort instanceof String text && !text.isBlank()) {
			screen.setSortOrd(Integer.parseInt(text.trim()));
		}
		if (screen.getScreenId() == null || screen.getScreenId().isBlank()) {
			throw new IllegalArgumentException("screenId는 필수입니다.");
		}
		if (screen.getScreenNm() == null || screen.getScreenNm().isBlank()) {
			throw new IllegalArgumentException("화면명은 필수입니다.");
		}
		if (screen.getUriPath() == null || screen.getUriPath().isBlank()) {
			throw new IllegalArgumentException("URI 경로는 필수입니다.");
		}
		return screen;
	}

	private static String asString(Object value) {
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value).trim();
		return text.isEmpty() || "null".equalsIgnoreCase(text) ? null : text;
	}

	private static ScreenTableMap resolveTableMap(Map<String, ScreenTableMap> tableByUri, String uriPath) {
		if (uriPath == null || uriPath.isBlank()) {
			return null;
		}
		ScreenTableMap mapped = tableByUri.get(DoPathHelper.normalizeForScreenLookup(uriPath));
		if (mapped != null) {
			return mapped;
		}
		return tableByUri.get(uriPath);
	}

	private Map<String, Object> toDto(ScreenList screen, ScreenTableMap mapped) {
		ScreenTableResolver.Mapping fallback = ScreenTableResolver.resolve(screen.getUriPath());
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("screenId", screen.getScreenId());
		row.put("screenNm", ScreenSidebarLoader.resolveDisplayName(screen.getScreenNm(), screen.getUriPath()));
		row.put("uriPath", screen.getUriPath());
		row.put("templatePath", screen.getTemplatePath());
		row.put("sortOrd", screen.getSortOrd());
		row.put("useYn", screen.getUseYn());
		row.put("menuGroup", screen.getMenuGroup());
		row.put("iconClass", screen.getIconClass());
		row.put("parentScreenId", screen.getParentScreenId());
		String linkPath = screen.getUriPath() != null
				? com.example.springbootapp.config.web.DoPathHelper.stripDoSuffix(screen.getUriPath())
				: "";
		if (linkPath.isEmpty() || "/index".equals(linkPath)) {
			linkPath = "/";
		}
		row.put("linkPath", linkPath);
		row.put("primaryTable", mapped != null ? mapped.getPrimaryTable() : fallback.primaryTable());
		row.put("relatedTables", mapped != null ? mapped.getRelatedTables() : fallback.relatedTables());
		row.put("dataType", mapped != null ? mapped.getDataType() : String.valueOf(fallback.dataType()));
		row.put("tableDesc", mapped != null ? mapped.getTableDesc() : fallback.tableDesc());
		row.put("regId", screen.getRegId());
		row.put("regDt", screen.getRegDt());
		row.put("updateId", screen.getUpdateId());
		row.put("updateDt", screen.getUpdateDt());
		return row;
	}
}
