package com.example.springbootapp.controller;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.service.UserAuthProfileService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-auth")
public class AdminUserAuthApiController {

	private final UserAuthProfileService userAuthProfileService;
	private final SessionAuthService sessionAuthService;

	public AdminUserAuthApiController(
			UserAuthProfileService userAuthProfileService,
			SessionAuthService sessionAuthService) {
		this.userAuthProfileService = userAuthProfileService;
		this.sessionAuthService = sessionAuthService;
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> get(@PathVariable String userId, HttpSession session) {
		requirePlatformAdmin(session);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("userId", userId);
		var access = userAuthProfileService.findAccess(userId);
		body.put("roleCd", access.getRoleCd());
		body.put("allowedCompanyIds", access.getAllowedCompanyIds());
		body.put("allowedMenuPaths", access.getAllowedMenuPaths());
		return ResponseEntity.ok(body);
	}

	@PutMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> save(
			@PathVariable String userId,
			@RequestBody Map<String, Object> body,
			HttpSession session) {
		requirePlatformAdmin(session);
		String roleCd = body.get("roleCd") != null ? String.valueOf(body.get("roleCd")) : null;
		@SuppressWarnings("unchecked")
		List<Long> companyIds = body.get("companyIds") instanceof List<?> list
				? (List<Long>) list
				: List.of();
		String actor = sessionAuthService.getLoginUserId(session);
		userAuthProfileService.saveProfile(userId, roleCd, companyIds, actor);
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("success", true);
		res.put("message", "사용자 권한이 저장되었습니다.");
		return ResponseEntity.ok(res);
	}

	private void requirePlatformAdmin(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null || login.getMenuAccess() == null
				|| login.getMenuAccess().role() != AppRole.PLATFORM_ADMIN) {
			throw new IllegalArgumentException("플랫폼 관리자 권한이 필요합니다.");
		}
	}
}
