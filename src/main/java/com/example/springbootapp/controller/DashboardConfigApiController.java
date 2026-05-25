package com.example.springbootapp.controller;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.service.DashboardCompanyConfigService;
import com.example.springbootapp.service.DashboardCompanySessionService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardConfigApiController {

	private final DashboardCompanyConfigService configService;
	private final DashboardCompanySessionService companySessionService;
	private final SessionAuthService sessionAuthService;

	public DashboardConfigApiController(
			DashboardCompanyConfigService configService,
			DashboardCompanySessionService companySessionService,
			SessionAuthService sessionAuthService) {
		this.configService = configService;
		this.companySessionService = companySessionService;
		this.sessionAuthService = sessionAuthService;
	}

	@GetMapping("/companies")
	public List<Map<String, Object>> companies() {
		return companySessionService.listActiveCompanies();
	}

	@GetMapping("/config")
	public ResponseEntity<Map<String, Object>> getConfig(
			@RequestParam Long companyId,
			HttpSession session) {
		companySessionService.setSelectedCompanyId(session, companyId);
		return ResponseEntity.ok(configService.getConfig(companyId));
	}

	@PutMapping("/config")
	public ResponseEntity<Map<String, Object>> saveConfig(
			@RequestBody Map<String, Object> body,
			HttpSession session) {
		Long companyId = parseCompanyId(body.get("companyId"));
		@SuppressWarnings("unchecked")
		List<String> hidden = body.get("hidden") instanceof List<?> list
				? (List<String>) list
				: List.of();
		String userId = resolveUserId(session);
		configService.saveConfig(companyId, hidden, userId);
		companySessionService.setSelectedCompanyId(session, companyId);
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("success", true);
		res.put("config", configService.getConfig(companyId));
		return ResponseEntity.ok(res);
	}

	@PutMapping("/selected-company")
	public ResponseEntity<Map<String, Object>> selectCompany(
			@RequestBody Map<String, Object> body,
			HttpSession session) {
		Long companyId = parseCompanyId(body.get("companyId"));
		companySessionService.setSelectedCompanyId(session, companyId);
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("success", true);
		res.put("companyId", companyId);
		res.put("companyNm", companySessionService.companyName(companyId));
		res.put("config", configService.getConfig(companyId));
		return ResponseEntity.ok(res);
	}

	private Long parseCompanyId(Object raw) {
		if (raw instanceof Number number) {
			return number.longValue();
		}
		if (raw instanceof String text && !text.isBlank()) {
			return Long.parseLong(text.trim());
		}
		throw new IllegalArgumentException("companyId가 필요합니다.");
	}

	private String resolveUserId(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		return login != null ? login.getUserId() : "SYSTEM";
	}
}
