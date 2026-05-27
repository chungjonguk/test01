package com.example.springbootapp.controller;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.service.CompanyCustomerMenuService;
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
@RequestMapping("/api/admin/company-customer-menus")
public class AdminCompanyCustomerMenuApiController {

	private final CompanyCustomerMenuService companyCustomerMenuService;
	private final DashboardCompanySessionService companySessionService;
	private final SessionAuthService sessionAuthService;

	public AdminCompanyCustomerMenuApiController(
			CompanyCustomerMenuService companyCustomerMenuService,
			DashboardCompanySessionService companySessionService,
			SessionAuthService sessionAuthService) {
		this.companyCustomerMenuService = companyCustomerMenuService;
		this.companySessionService = companySessionService;
		this.sessionAuthService = sessionAuthService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) Long companyId,
			HttpSession session) {
		requireCompanyAdmin(session);
		Long resolved = companyId != null ? companyId : companySessionService.resolveSelectedCompanyId(session);
		Map<String, Object> body = companyCustomerMenuService.listForCompany(resolved);
		body.put("success", true);
		return ResponseEntity.ok(body);
	}

	@PutMapping
	public ResponseEntity<Map<String, Object>> save(
			@RequestBody Map<String, Object> body,
			HttpSession session) {
		requireCompanyAdmin(session);
		Long companyId = parseCompanyId(body.get("companyId"));
		if (companyId == null) {
			companyId = companySessionService.resolveSelectedCompanyId(session);
		}
		@SuppressWarnings("unchecked")
		List<String> menuPaths = body.get("menuPaths") instanceof List<?> list
				? (List<String>) list
				: List.of();
		companyCustomerMenuService.saveMenus(companyId, menuPaths, session);
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("success", true);
		res.put("message", "고객 노출 메뉴가 저장되었습니다.");
		res.putAll(companyCustomerMenuService.listForCompany(companyId));
		return ResponseEntity.ok(res);
	}

	private void requireCompanyAdmin(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null || login.getMenuAccess() == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		AppRole role = login.getMenuAccess().role();
		if (role != AppRole.PLATFORM_ADMIN && role != AppRole.COMPANY_ADMIN) {
			throw new IllegalArgumentException("업체 관리 권한이 필요합니다.");
		}
	}

	private static Long parseCompanyId(Object raw) {
		if (raw instanceof Number number) {
			return number.longValue();
		}
		if (raw instanceof String text && !text.isBlank()) {
			return Long.parseLong(text.trim());
		}
		return null;
	}
}
