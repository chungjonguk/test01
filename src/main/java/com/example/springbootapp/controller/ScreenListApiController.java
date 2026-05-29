package com.example.springbootapp.controller;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.service.ScreenListService;
import jakarta.servlet.http.HttpSession;
/**
 * 화면(스크린) 목록 REST API.
 * <p>기본 경로: {@code /api/screens}</p>
 */
@RestController
@RequestMapping("/api/screens")
public class ScreenListApiController {
	private final ScreenListService screenListService;
	private final SessionAuthService sessionAuthService;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public ScreenListApiController(
			ScreenListService screenListService,
			SessionAuthService sessionAuthService,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.screenListService = screenListService;
		this.sessionAuthService = sessionAuthService;
		this.publicPathCrypto = publicPathCrypto;
	}
	/**
	 * 활성화된 전체 화면 목록을 조회합니다.
	 *
	 * @return out: {@code ResponseEntity<Map>} — {@code screens}, {@code count}
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> list(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		List<Map<String, Object>> rows = screenListService.findAllActive().stream()
				.filter(screen -> isVisibleToUser(screen, login))
				.map(this::toDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("count", rows.size());
		body.put("screens", rows);
		return ResponseEntity.ok(body);
	}
	/**
	 * 화면 ID로 단건 정보를 조회합니다.
	 *
	 * @param screenId in: 화면 ID
	 * @return out: {@code ResponseEntity<Map>} — 화면 필드 또는 404
	 */
	@GetMapping("/{screenId}")
	public ResponseEntity<Map<String, Object>> one(@PathVariable String screenId, HttpSession session) {
		ScreenList screen = screenListService.findByScreenId(screenId);
		if (screen == null) {
			return ResponseEntity.notFound().build();
		}
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (!isVisibleToUser(screen, login)) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(toDto(screen));
	}

	private boolean isVisibleToUser(ScreenList screen, LoginSession login) {
		if (login == null || login.getMenuAccess() == null) {
			return false;
		}
		MenuAccessSnapshot access = login.getMenuAccess();
		if (access.isWriteAll() || access.role() == AppRole.PLATFORM_ADMIN) {
			return true;
		}
		return access.isMenuAllowed(toLinkPath(screen.getUriPath()));
	}
	private Map<String, Object> toDto(ScreenList screen) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("screenId", screen.getScreenId());
		row.put("screenNm", screen.getScreenNm());
		row.put("uriPath", screen.getUriPath());
		row.put("templatePath", screen.getTemplatePath());
		row.put("sortOrd", screen.getSortOrd());
		row.put("useYn", screen.getUseYn());
		String linkPath = toLinkPath(screen.getUriPath());
		row.put("linkPath", linkPath);
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			row.put("href", crypto.toPublicPath(linkPath));
		} else {
			row.put("href", DoPathHelper.toDoPath(linkPath));
		}
		return row;
	}

	private static String toLinkPath(String uriPath) {
		String path = DoPathHelper.stripDoSuffix(uriPath != null ? uriPath : "");
		if (path.isEmpty() || "/index".equals(path)) {
			return "/";
		}
		return path;
	}
}
