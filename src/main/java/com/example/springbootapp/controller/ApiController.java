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
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.storage.NasStorageService.NasStoredFile;
import jakarta.servlet.http.HttpSession;
/**
 * 공통 REST API (서버 상태, 파일 업로드).
 * <p>기본 경로: {@code /api}</p>
 */
@RestController
@RequestMapping("/api")
public class ApiController {
	private final NasStorageService nasStorageService;
	private final SessionAuthService sessionAuthService;
	public ApiController(NasStorageService nasStorageService, SessionAuthService sessionAuthService) {
		this.nasStorageService = nasStorageService;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 서버 실행 상태를 확인합니다.
	 *
	 * @return out: {@code ResponseEntity<Map>} — {@code status}, {@code timestamp}, {@code message}
	 */
	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status() {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "running");
		response.put("timestamp", LocalDateTime.now());
		response.put("message", "서버가 정상적으로 실행 중입니다.");
		return ResponseEntity.ok(response);
	}
	/**
	 * 파일을 NAS 스토리지에 업로드합니다.
	 *
	 * @param file    in: 업로드할 멀티파트 파일
	 * @param type    in: 미디어 유형 코드 (기본 image)
	 * @param session in: 등록자 식별용 HTTP 세션
	 * @return out: {@code ResponseEntity<Map>} — 성공 시 {@code success}, {@code fileId}, {@code url} 등 또는 400 시 {@code error}
	 */
	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "type", defaultValue = "image") String type,
			HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		if (file == null || file.isEmpty()) {
			response.put("error", "파일이 비어 있습니다.");
			return ResponseEntity.badRequest().body(response);
		}
		NasMediaType mediaType = NasMediaType.fromCode(type).orElse(NasMediaType.IMAGE);
		try {
			String regId = sessionAuthService.getLoginUserId(session);
			if (regId == null || regId.isBlank()) {
				regId = "SYSTEM";
			}
			NasStoredFile stored = nasStorageService.store(mediaType, file, regId);
			response.put("success", true);
			response.put("fileId", stored.fileId());
			response.put("name", stored.originalName());
			response.put("filename", stored.filename());
			response.put("url", stored.url());
			response.put("filePath", stored.filePath());
			response.put("type", stored.type().name().toLowerCase());
			response.put("size", stored.sizeBytes());
			response.put("message", "업로드되었습니다.");
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException | java.io.IOException ex) {
			response.put("error", ex.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}
}
