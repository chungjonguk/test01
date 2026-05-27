package com.example.springbootapp.controller;

import com.example.springbootapp.service.UserProfileImageService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/profile-images")
public class UserProfileImageApiController {

	private final UserProfileImageService userProfileImageService;

	public UserProfileImageApiController(UserProfileImageService userProfileImageService) {
		this.userProfileImageService = userProfileImageService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> get(HttpSession session) {
		try {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.putAll(userProfileImageService.getForSession(session));
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PostMapping("/profile")
	public ResponseEntity<Map<String, Object>> uploadProfile(
			@RequestParam("file") MultipartFile file,
			HttpSession session) {
		return upload(file, session, true);
	}

	@PostMapping("/cover")
	public ResponseEntity<Map<String, Object>> uploadCover(
			@RequestParam("file") MultipartFile file,
			HttpSession session) {
		return upload(file, session, false);
	}

	private ResponseEntity<Map<String, Object>> upload(
			MultipartFile file,
			HttpSession session,
			boolean profile) {
		try {
			Map<String, Object> image = profile
					? userProfileImageService.uploadProfile(session, file)
					: userProfileImageService.uploadCover(session, file);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("image", image);
			body.put("message", profile ? "프로필 이미지가 등록되었습니다." : "커버 이미지가 등록되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		} catch (IOException ex) {
			return badRequest(ex.getMessage() != null ? ex.getMessage() : "파일 저장에 실패했습니다.");
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
