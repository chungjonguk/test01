package com.example.springbootapp.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiController {

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status() {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "running");
		response.put("timestamp", LocalDateTime.now());
		response.put("message", "서버가 정상적으로 실행 중입니다.");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
		Map<String, Object> response = new HashMap<>();
		if (file == null || file.isEmpty()) {
			response.put("error", "파일이 비어 있습니다.");
			return ResponseEntity.badRequest().body(response);
		}
		response.put("name", file.getOriginalFilename());
		response.put("size", file.getSize());
		response.put("message", "업로드되었습니다.");
		return ResponseEntity.ok(response);
	}
}
