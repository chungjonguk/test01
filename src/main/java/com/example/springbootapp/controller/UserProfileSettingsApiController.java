package com.example.springbootapp.controller;

import com.example.springbootapp.dto.UserProfileSettingsDto;
import com.example.springbootapp.service.UserProfileSettingsService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile-settings")
public class UserProfileSettingsApiController {

	private final UserProfileSettingsService userProfileSettingsService;

	public UserProfileSettingsApiController(UserProfileSettingsService userProfileSettingsService) {
		this.userProfileSettingsService = userProfileSettingsService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> get(HttpSession session) {
		try {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.putAll(userProfileSettingsService.getForSession(session));
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PutMapping
	public ResponseEntity<Map<String, Object>> update(
			@RequestBody UserProfileSettingsDto dto,
			HttpSession session) {
		try {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.putAll(userProfileSettingsService.update(session, dto));
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		} catch (IllegalStateException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
