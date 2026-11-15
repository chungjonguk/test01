package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.dto.UserRegisterDto;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.service.UserAccessLogService;
import com.example.springbootapp.service.UserAuthProfileService;
import com.example.springbootapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * SPA용 세션 인증 JSON API — {@code /api/auth}
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final UserMapper userMapper;
	private final SessionAuthService sessionAuthService;
	private final UserAccessLogService userAccessLogService;
	private final UserService userService;
	private final UserAuthProfileService userAuthProfileService;

	public AuthApiController(
			UserMapper userMapper,
			SessionAuthService sessionAuthService,
			UserAccessLogService userAccessLogService,
			UserService userService,
			UserAuthProfileService userAuthProfileService) {
		this.userMapper = userMapper;
		this.sessionAuthService = sessionAuthService;
		this.userAccessLogService = userAccessLogService;
		this.userService = userService;
		this.userAuthProfileService = userAuthProfileService;
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("ok", true);
		body.put("message", "PrintMall backend");
		return ResponseEntity.ok(body);
	}

	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> me(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("authenticated", false);
			body.put("message", "로그인이 필요합니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("authenticated", true);
		body.put("user", toUserDto(login));
		return ResponseEntity.ok(body);
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@RequestBody Map<String, Object> payload,
			HttpServletRequest request) {
		String id = stringVal(payload.get("id"));
		if (id.isEmpty()) {
			id = stringVal(payload.get("userId"));
		}
		if (id.isEmpty()) {
			id = stringVal(payload.get("email"));
		}
		String pw = stringVal(payload.get("pw"));
		if (pw.isEmpty()) {
			pw = stringVal(payload.get("password"));
		}

		if (id.isEmpty() || pw.isEmpty()) {
			userAccessLogService.recordLogin(request, null, "FORM", false, "입력값 없음");
			return fail(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 입력하세요.");
		}

		Optional<User> user = userMapper.findById(id);
		if (user.isEmpty() || !pw.equals(user.get().getPw())) {
			userAccessLogService.recordLogin(request, user.orElse(null), "FORM", false, "인증 실패");
			return fail(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 잘못 되었습니다.");
		}

		sessionAuthService.loginFromUser(request.getSession(true), user.get(), request);
		LoginSession login = sessionAuthService.getLoginSession(request.getSession(false));
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("authenticated", true);
		body.put("message", user.get().getName() + "님, 로그인되었습니다.");
		body.put("user", toUserDto(login != null ? login : sessionFromUser(user.get())));
		return ResponseEntity.ok(body);
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		sessionAuthService.logout(session, request);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("authenticated", false);
		body.put("message", "로그아웃되었습니다.");
		return ResponseEntity.ok(body);
	}

	@GetMapping("/check-id")
	public ResponseEntity<Map<String, Object>> checkId(@RequestParam(required = false) String id) {
		String trimmed = stringVal(id);
		Map<String, Object> body = new LinkedHashMap<>();
		if (trimmed.isEmpty()) {
			body.put("available", false);
			body.put("message", "아이디를 입력해 주세요.");
			return ResponseEntity.ok(body);
		}
		boolean available = !userService.existsById(trimmed);
		body.put("available", available);
		body.put("message", available ? "사용 가능한 아이디입니다." : "이미 등록된 아이디입니다.");
		return ResponseEntity.ok(body);
	}

	@GetMapping("/check-email")
	public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam(required = false) String email) {
		String trimmed = stringVal(email);
		Map<String, Object> body = new LinkedHashMap<>();
		if (trimmed.isEmpty()) {
			body.put("available", false);
			body.put("message", "이메일을 입력해 주세요.");
			return ResponseEntity.ok(body);
		}
		if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
			body.put("available", false);
			body.put("message", "올바른 이메일 형식이 아닙니다.");
			return ResponseEntity.ok(body);
		}
		boolean available = !userService.existsByEmail(trimmed);
		body.put("available", available);
		body.put("message", available ? "사용 가능한 이메일입니다." : "이미 등록된 이메일입니다.");
		return ResponseEntity.ok(body);
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> payload) {
		UserRegisterDto dto = new UserRegisterDto();
		dto.setId(stringVal(first(payload, "id", "userId")));
		dto.setPw(stringVal(first(payload, "pw", "password")));
		dto.setName(stringVal(payload.get("name")));
		dto.setSex(stringVal(payload.get("sex")));
		dto.setRrno(stringVal(first(payload, "rrno", "residentNo")));
		dto.setEmail(stringVal(payload.get("email")));
		dto.setZipcode(stringVal(first(payload, "zipcode", "zipCode")));
		dto.setAddress(stringVal(payload.get("address")));
		dto.setAddressDetail(stringVal(first(payload, "addressDetail", "address_detail")));
		try {
			userService.register(dto);
			userAuthProfileService.saveProfile(dto.getId(), AppRole.COMPANY_CUSTOMER.name(), null, dto.getId());
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("message", "회원가입이 완료되었습니다.");
			body.put("userId", dto.getId());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return fail(HttpStatus.BAD_REQUEST, ex.getMessage());
		} catch (RuntimeException ex) {
			return fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다.");
		}
	}

	@PostMapping("/find-id")
	public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, Object> payload) {
		String name = stringVal(payload.get("name"));
		String email = stringVal(payload.get("email"));
		if (name.isEmpty() || email.isEmpty()) {
			return fail(HttpStatus.BAD_REQUEST, "이름과 이메일을 모두 입력하세요.");
		}
		Optional<String> loginId = userMapper.findLoginIdByNameAndEmail(name, email);
		if (loginId.isEmpty()) {
			return fail(HttpStatus.NOT_FOUND, "일치하는 회원 정보가 없습니다. 이름과 이메일을 확인하세요.");
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("loginId", maskLoginId(loginId.get()));
		body.put("message", "아이디를 찾았습니다.");
		return ResponseEntity.ok(body);
	}

	private LoginSession sessionFromUser(User user) {
		LoginSession login = new LoginSession();
		login.setUserId(user.getId());
		login.setUserName(user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getId());
		login.setEmail(user.getEmail());
		login.setLoginType("FORM");
		return login;
	}

	private Map<String, Object> toUserDto(LoginSession login) {
		Map<String, Object> user = new LinkedHashMap<>();
		user.put("userId", login.getUserId());
		user.put("userName", login.getUserName());
		user.put("email", login.getEmail());
		user.put("loginType", login.getLoginType());
		user.put("loginAt", login.getLoginAt() != null ? login.getLoginAt().toString() : null);
		MenuAccessSnapshot access = login.getMenuAccess();
		if (access != null) {
			user.put("roleCd", access.getRoleCd());
			user.put("writeAll", access.isWriteAll());
			user.put("allowedMenuPaths", access.getAllowedMenuPaths());
		} else {
			user.put("roleCd", null);
			user.put("writeAll", false);
			user.put("allowedMenuPaths", java.util.List.of());
		}
		return user;
	}

	private static String stringVal(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static Object first(Map<String, Object> payload, String... keys) {
		for (String key : keys) {
			if (payload.containsKey(key) && payload.get(key) != null) {
				return payload.get(key);
			}
		}
		return null;
	}

	private static String maskLoginId(String loginId) {
		if (loginId == null || loginId.isBlank()) {
			return "";
		}
		String id = loginId.trim();
		if (id.length() <= 2) {
			return id.charAt(0) + "*";
		}
		if (id.length() <= 4) {
			return id.substring(0, 1) + "*".repeat(id.length() - 1);
		}
		return id.substring(0, 2) + "*".repeat(Math.max(1, id.length() - 4)) + id.substring(id.length() - 2);
	}

	private static ResponseEntity<Map<String, Object>> fail(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("authenticated", false);
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}
