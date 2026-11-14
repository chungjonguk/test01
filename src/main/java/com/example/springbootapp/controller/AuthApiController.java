package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.service.UserAccessLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * SPA용 세션 인증 JSON API — {@code /api/auth}
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

	private final UserMapper userMapper;
	private final SessionAuthService sessionAuthService;
	private final UserAccessLogService userAccessLogService;

	public AuthApiController(
			UserMapper userMapper,
			SessionAuthService sessionAuthService,
			UserAccessLogService userAccessLogService) {
		this.userMapper = userMapper;
		this.sessionAuthService = sessionAuthService;
		this.userAccessLogService = userAccessLogService;
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

	private static ResponseEntity<Map<String, Object>> fail(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("authenticated", false);
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}
