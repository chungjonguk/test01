package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.dto.SocialNotificationFormDto;
import com.example.springbootapp.service.SocialNotificationService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/social/notifications")
public class SocialNotificationApiController {

	private final SocialNotificationService socialNotificationService;

	public SocialNotificationApiController(SocialNotificationService socialNotificationService) {
		this.socialNotificationService = socialNotificationService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) String userNm,
			@RequestParam(required = false) String senderNm,
			@RequestParam(required = false) String readYn,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false, defaultValue = "false") boolean grid,
			@RequestParam(required = false, defaultValue = "0") int limit,
			HttpSession session) {
		List<Map<String, Object>> items = grid
				? socialNotificationService.searchForGrid(userNm, senderNm, readYn, keyword)
				: socialNotificationService.listForApi(session, limit);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("items", items);
		body.put("count", items.size());
		body.put("unread", items.stream().filter(item -> Boolean.TRUE.equals(item.get("unread"))).count());
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(
			@RequestBody SocialNotificationFormDto dto,
			HttpSession session) {
		try {
			Long id = socialNotificationService.create(dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("notificationId", id);
			body.put("message", "알림이 등록되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long notificationId) {
		try {
			socialNotificationService.markRead(notificationId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long notificationId) {
		try {
			socialNotificationService.delete(notificationId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("message", "알림이 삭제되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PatchMapping("/read-all")
	public ResponseEntity<Map<String, Object>> markAllRead(HttpSession session) {
		int updated = socialNotificationService.markAllRead(session);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("updated", updated);
		return ResponseEntity.ok(body);
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
